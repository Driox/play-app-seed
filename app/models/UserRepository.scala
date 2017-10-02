package models

import models.dao.DbDriver.DbProfile
import org.joda.time.DateTime
import models.dao.{TableHelper, TableMapping}
import play.api.db.slick.HasDatabaseConfigProvider

trait UserComponent extends TableMapping {
  self: HasDatabaseConfigProvider[DbProfile] =>

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

    def * = (id, uuid, created_at, deleted_at, email, password, first_name, last_name, avatar_url, birthday, phone, language) <> (User.tupled, User.unapply _)
  }

}
