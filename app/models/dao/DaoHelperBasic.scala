package models.dao

import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig
import slick.driver.JdbcProfile
import play.api.Play
import com.github.tototoshi.slick.JdbcJodaSupport._
import driver.api._

// Entity with id
trait Entity[E] {
  def id: Option[Long]
  def copyWithId(id: Option[Long]): E
}

trait TableHelper {
  def id: Rep[Option[Long]]
}

// DaoHelper generic for model with id
trait DaoHelperBasic[T <: Table[E] with TableHelper, E <: Entity[E]] extends HasDatabaseConfig[JdbcProfile] {

  //----------------
  // to override
  //----------------

  val tables: TableQuery[T]

  //----------------
  // config
  //----------------

  protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  def createTable() = db.run(tables.schema.create)
  def dropTable() = db.run(tables.schema.drop)
}
