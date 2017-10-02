package models.dao

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.relational.RelationalProfile

import scala.concurrent.{ExecutionContext, Future}
import slick.lifted._

trait CrudRepository[E <: Entity[E], J <: JdbcProfile] { self: HasDatabaseConfigProvider[J] =>

  import profile.api._

  type TableType <: TableHelper with RelationalProfile#Table[E]
  def tables: TableQuery[TableType]

  /**
   * Finds all entities.
   */
  def all()(implicit ec: ExecutionContext): Future[List[E]] = {
    db.run(tableQueryCompiled.result)
  }

  def findById(id: String)(implicit ec: ExecutionContext): Future[Option[E]] = {
    db.run(byId(id).result.headOption)
  }

  def findByIds(ids: Seq[String])(implicit ec: ExecutionContext): Future[List[E]] = {
    db.run(tables.filter(_.id inSet ids).to[List].result)
  }

  def create(entity: E)(implicit ec: ExecutionContext): Future[E] = {
    val id = entity.id
    db.run(tables += entity).flatMap(_ => findById(id).map(_.get))
  }

  def create(entities: Seq[E])(implicit ec: ExecutionContext): Future[List[E]] = {
    val ids = entities.map(_.id)
    db.run(tables ++= entities).flatMap(_ => findByIds(ids))
  }

  def update(entity: E)(implicit ec: ExecutionContext): Future[Option[E]] = {
    update(entity.id, entity)
  }

  protected def update(id: String, entity: E)(implicit ec: ExecutionContext): Future[Option[E]] = {
    val action = (for {
      _ <- byId(id).update(entity)
      result <- byId(id).result.headOption
    } yield (result)).transactionally
    db.run(action)
  }

  def delete(id: String)(implicit ec: ExecutionContext): Future[Int] = {
    delete(Seq(id))
  }

  def delete(ids: Seq[String])(implicit ec: ExecutionContext): Future[Int] = {
    db.run(tables.filter(_.id inSet ids).delete)
  }

  lazy protected val byId = Compiled { id: Rep[String] => tables.filter(_.id === id) }
  // can't compile inSet for now : https://github.com/slick/slick/issues/718
  //lazy protected val byIds = Compiled { ids: Rep[Traversable[String]] => tables.filter(_.id inSet ids).to[List] }
  lazy protected val tableQueryCompiled = Compiled(tables.to[List])
  lazy protected val saveCompiled = tables returning tables.map(_.id)
  lazy private val countCompiled = Compiled(tables.map(_.id).length)
}
