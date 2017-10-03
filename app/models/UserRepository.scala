package models

import org.joda.time.DateTime
import models.dao._
import play.api.db.slick.HasDatabaseConfig
import play.api.libs.json.JsValue
import slick.jdbc.JdbcProfile

trait UserComponent extends TableMapping {
  self: HasDatabaseConfig[JdbcProfile] =>

  import profile.api._

  class UserTable(tag: Tag) extends Table[User](tag, "users") with TableHelper {

    val id = column[String]("id", O.PrimaryKey)
    val uuid = column[String]("uuid")
    val created_at = column[DateTime]("created_at")
    val deleted_at = column[Option[DateTime]]("deleted_at")
    val email = column[String]("email")
    val password = column[String]("password")
    val first_name = column[Option[String]]("first_name")
    val last_name = column[Option[String]]("last_name")
    val avatar_url = column[Option[String]]("avatar_url")
    val birthday = column[Option[DateTime]]("birthday")
    val phone = column[Option[String]]("phone")
    val language = column[Option[String]]("language")
    //    val custom = column[Option[JsValue]]("custom")

    def * = (id, uuid, created_at, deleted_at, email, password, first_name, last_name, avatar_url, birthday, phone, language /*, custom*/ ) <> (User.tupled, User.unapply _)
  }

}
