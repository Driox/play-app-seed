package models

import models.dao._
import play.api.db.slick._
import play.api.libs.json.JsValue
import slick.jdbc.JdbcProfile
import utils.{ StringUtils, TimeUtils }

import java.time.OffsetDateTime
import javax.inject._
import scala.concurrent.{ ExecutionContext, Future }

/**
 * easy to mock :
 *  val mockUser = new User(email, password) with UserAuthMock
 *
 * easy to user :
 *  user.authenticate(credentials)
 */
case class User(
  id:         String                 = StringUtils.generateUuid(),
  uuid:       String                 = StringUtils.generateUuid(),
  created_at: OffsetDateTime         = TimeUtils.now,
  deleted_at: Option[OffsetDateTime] = None,
  email:      String,
  password:   String,
  first_name: Option[String]         = None,
  last_name:  Option[String]         = None,
  avatar_url: Option[String]         = None,
  birthday:   Option[OffsetDateTime] = None,
  phone:      Option[String]         = None,
  language:   Option[String]         = None,
  custom:     Option[JsValue]        = None
) extends UserAuth
    with Entity[User] {
  def copyWithId(id: String) = this.copy(id = id)
}

case class Credentials(email: String, password: String)

trait UserAuth {
  self: User =>

  def authenticate(credentials: Credentials): Boolean = {
    email == credentials.email && password == credentials.password
  }

  def isAuthorized(): Boolean = ???
}

@Singleton
class Users @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile]
    with CrudRepository[User, JdbcProfile]
    with EntityWithTableLifecycle[JdbcProfile]
    with UserComponent {

  import profile.api._

  val tables = TableQuery[UserTable]
  type TableType = UserTable

  def findByEmail(email: String)(implicit executionCtx: ExecutionContext): Future[Option[User]] = {
    db.run(tables.filter(_.email === email).result.headOption)
  }

  def findByUuid(uuid: String)(implicit executionCtx: ExecutionContext): Future[Option[User]] = {
    val q = tables.filter(_.uuid === uuid)
    db.run(q.result.headOption)
  }

  def create(email: String, password: String)(implicit executionCtx: ExecutionContext): Future[User] = {
    create(User(email = email, password = password))
  }

}
