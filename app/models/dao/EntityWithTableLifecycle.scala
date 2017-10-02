package models.dao

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted._

// Entity with id
trait Entity[E] {
  def id: String
  def copyWithId(id: String): E
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
