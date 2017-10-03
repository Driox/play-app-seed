package models.dao

import play.api.libs.json.JsValue
import slick.jdbc.{H2Profile, JdbcProfile, PostgresProfile}
import com.github.tototoshi.slick.GenericJodaSupport

object MetaProfile {

  type DbProfile = GenericJodaSupport with JdbcProfile with CollectionSupport with JsonSupport

  lazy val db_driver: DbProfile = {
    if (helpers.Config.isDev()) {
      EnhancedH2Driver
    } else {
      PostgreSqlDriver
    }
  }
}

trait JsonSupport {

  implicit def jsonToStringMapper: JdbcProfile#BaseColumnType[JsValue]
  implicit def GetJsValue: slick.jdbc.GetResult[JsValue]
  implicit def GetJsValueOption: slick.jdbc.GetResult[Option[JsValue]]
}

trait CollectionSupport { self: GenericJodaSupport =>

  import self.driver.api._
  implicit val mapTypeMapper = EnhancedPostgresDriver.api.simpleHStoreTypeMapper
  implicit val seqTypeMapper = MappedColumnType.base[Seq[String], String](
    list => list.map(_.replaceAll(";", ",,")) mkString ";",
    str => (str split ";").toList.map(_.replaceAll(",,", ";"))
  )
}

object DbDriver {

  trait DbProfile extends GenericJodaSupport with JdbcProfile

  class PostgreSqlDriver
    extends GenericJodaSupport(PostgresProfile)
    with EnhancedPostgresDriver
    with DbProfile

  //  with PostGreEnumSupport
  //  with JsonSupport
  //  with CollectionSupport

  class H2SqlDriver extends GenericJodaSupport(H2Profile)
    with DbProfile

  //  with H2EnumSupport
  //  with JsonSupport
  //  with CollectionSupport

}