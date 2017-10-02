package models

import javax.inject._

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import org.joda.time.DateTime
import utils.DateUtils
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
    id:         String           = StringUtils.generateUuid,
    uuid:       String           = StringUtils.generateUuid,
    created_at: Option[DateTime] = Some(DateUtils.now),
    deleted_at: Option[DateTime] = None,
    email:      String,
    password:   String,
    first_name: Option[String]   = None,
    last_name:  Option[String]   = None,
    avatar_url: Option[String]   = None,
    birthday:   Option[DateTime] = None,
    phone:      Option[String]   = None,
    language:   Option[String]   = None
) extends UserAuth with Entity[User] {
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
    extends HasDatabaseConfigProvider[EnhancedPostgresDriver]
    with CrudRepository[User, EnhancedPostgresDriver]
    with EntityWithTableLifecycle[EnhancedPostgresDriver]
    with UserComponent {

  import profile.api._

  val tables = TableQuery[UserTable]
  type TableType = UserTable

  def findByEmail(email: String)(implicit executionCtx: ExecutionContext): Future[Option[User]] = {
    db.run(tables.filter(_.email === email).result.headOption)
  }

  def findByUuid(uuid: String): Future[Option[User]] = {
    val q = tables.filter(_.uuid === uuid)
    db.run(q.result.headOption)
  }

  def create(email: String, password: String): Future[Option[User]] = ???

}
