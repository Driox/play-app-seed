package models.dao

import play.api.db.slick.HasDatabaseConfig
import play.api.libs.json.JsValue
import slick.jdbc.JdbcProfile
import slick.lifted._

// Entity with id
trait Entity[E] {
  def id: String
  def copyWithId(id: String): E
}

trait TableMapping {

  protected def profile: JdbcProfile

  protected val jsonTypeMapper = new JsonMapping(profile)

  implicit val jsonToStringMapper: JdbcProfile#BaseColumnType[JsValue] = jsonTypeMapper.jsonToStringMapper
  implicit val getJsValue                                              = jsonTypeMapper.GetJsValue
  implicit val getJsValueOption                                        = jsonTypeMapper.GetJsValueOption
}

trait TableHelper {
  def id: Rep[String]
}

trait EntityWithTableLifecycle[J <: JdbcProfile] { self: HasDatabaseConfig[J] =>

  import profile.api._

  def tables: TableQuery[_]

  def createTable() = db.run(profile.buildTableSchemaDescription(tables.shaped.value.asInstanceOf[Table[_]]).create)
  def dropTable()   = db.run(profile.buildTableSchemaDescription(tables.shaped.value.asInstanceOf[Table[_]]).drop)
}
