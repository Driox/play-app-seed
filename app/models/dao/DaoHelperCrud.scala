package models.dao

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile
import play.api.Play
import models.dao.PortableJodaSupport._
import driver.api._
import scala.concurrent.Future
import slick.lifted.AppliedCompiledFunction

// DaoHelper generic for model with id
trait DaoHelperCrud[T <: Table[E] with TableHelper, E <: Entity[E]] extends DaoHelperBasic[T, E] {

  protected def basic: Query[T, T#TableElementType, Seq] = tables

  //----------------
  //Compiled Queries
  //----------------

  private implicit class QueryExtensions[C](val q: Query[T, T#TableElementType, Seq]) {
    def byId(id: Rep[String]): Query[T, T#TableElementType, Seq] = q.filter(_.id === id)
    def byIds(ids: Traversable[String]): Query[T, T#TableElementType, Seq] = q.filter(_.id inSet ids)
  }

  protected val byId = Compiled { id: Rep[String] => basic.filter(_.id === id) }

  //---------------
  //Methods
  //---------------

  def all(): Future[Seq[E]] = {
    db.run(tables.result)
  }

  def findById(id: String): Future[Option[E]] = {
    db.run(byId(id).result.headOption)
  }

  def create(entity: E): Future[E] = {
    db.run((tables returning tables.map(_.id) into ((Entity, id) => Entity.copyWithId(id))) += entity)
  }

  def create(entities: Seq[E]): Future[Seq[E]] = {
    db.run((basic returning tables.map(_.id) into ((Entity, id) => Entity.copyWithId(id))) ++= entities)
  }

  def update(entity: E): Future[Option[E]] = {
    update(entity.id, entity)
  }

  protected def update(id: String, entity: E): Future[Option[E]] = {
    val action = (for {
      _ <- byId(id).update(entity)
      result <- byId(id).result.headOption
    } yield (result)).transactionally
    db.run(action)
  }

  def delete(id: String): Future[Int] = {
    delete(Seq(id))
  }

  def delete(ids: Seq[String]): Future[Int] = {
    db.run(basic.byIds(ids).delete)
  }

  protected def updateField[V, X](value: V, q: Query[Rep[V], V, Seq],
                                  compiledQuery: AppliedCompiledFunction[X, Query[T, E, Seq], Seq[E]]): Future[Option[E]] = {
    val action = (for {
      _ <- q.update(value)
      result <- compiledQuery.result.headOption
    } yield (result)).transactionally

    db.run(action)
  }

}
