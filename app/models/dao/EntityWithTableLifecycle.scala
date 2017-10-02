package models.dao

import com.github.tototoshi.slick.GenericJodaSupport
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted._

// Entity with id
trait Entity[E] {
  def id: String
  def copyWithId(id: String): E
}

trait TableMapping {

  protected def profile(): JdbcProfile

  protected val joda_mapping = new GenericJodaSupport(profile)

  implicit val datetimeTypeMapper = joda_mapping.datetimeTypeMapper
  implicit val getDatetimeResult = joda_mapping.getDatetimeResult
  implicit val getDatetimeOptionResult = joda_mapping.getDatetimeOptionResult
  implicit val setDatetimeParameter = joda_mapping.setDatetimeParameter
  implicit val setDatetimeOptionParameter = joda_mapping.setDatetimeOptionParameter
}

trait TableHelper {
  def id: Rep[String]
}

trait EntityWithTableLifecycle[J <: JdbcProfile] { self: HasDatabaseConfigProvider[J] =>

  import profile.api._

  def tables: TableQuery[_]

  def createTable() = db.run(profile.buildTableSchemaDescription(tables.shaped.value.asInstanceOf[Table[_]]).create)
  def dropTable() = db.run(profile.buildTableSchemaDescription(tables.shaped.value.asInstanceOf[Table[_]]).drop)
}
