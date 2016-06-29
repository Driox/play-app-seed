package models

import models.dao.PortableJodaSupport._
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider

import driver.api._
import org.joda.time.DateTime
import slick.lifted._
import slick.driver.JdbcProfile
import utils.StringUtils

import scala.concurrent.{ExecutionContext, Future}
import models.dao._

/**
 * easy to mock :
 *  val mockUser = new User(email, password) with UserAuthMock
 *
 * easy to user :
 *  user.authenticate(credentials)
 */
case class User(
    id:         Option[String]   = Some(StringUtils.generateUuid),
    uuid:       String           = StringUtils.generateUuid,
    created_at: Option[DateTime] = Some(DateTime.now),
    email:      String,
    password:   String,
    first_name: Option[String]   = None,
    last_name:  Option[String]   = None,
    avatar_url: Option[String]   = None,
    birthday:   Option[DateTime] = None,
    phone:      Option[String]   = None,
    language:   Option[String]   = None,
    visible:    Option[Boolean]  = None
) extends UserAuth with Entity[User] {
  def copyWithId(id: Option[String]) = this.copy(id = id)
}

case class Credentials(email: String, password: String)

trait UserAuth {
  self: User =>

  def authenticate(credentials: Credentials): Boolean = {
    email == credentials.email && password == credentials.password
  }

  def isAuthorized(): Boolean = ???
}

class UserTable(tag: Tag) extends Table[User](tag, "users") with TableHelper {

  val id = column[Option[String]]("id", O.PrimaryKey)
  val uuid = column[String]("uuid")
  val created_at = column[Option[DateTime]]("created_at")
  val email = column[String]("email")
  val password = column[String]("password")
  val first_name = column[Option[String]]("first_name")
  val last_name = column[Option[String]]("last_,ame")
  val avatar_url = column[Option[String]]("avatar_url")
  val birthday = column[Option[DateTime]]("birthday")
  val phone = column[Option[String]]("phone")
  val language = column[Option[String]]("language")
  val visible = column[Option[Boolean]]("visible")

  def * = (id, uuid, created_at, email, password, first_name, last_name, avatar_url, birthday, phone, language, visible) <> (User.tupled, User.unapply _)
}

class Users extends DaoHelperCrud[UserTable, User] {

  protected override val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import driver.api._

  override val tables = TableQuery[UserTable]

  def findByEmail(email: String)(implicit executionCtx: ExecutionContext): Future[Option[User]] = {
    db.run(tables.filter(_.email === email).result.headOption)
  }

  def findByUuid(uuid: String): Future[Option[User]] = {
    val q = tables.filter(_.uuid === uuid)
    db.run(q.result.headOption)
  }

  def create(email: String, password: String): Future[Option[User]] = ???

}
