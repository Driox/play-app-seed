package models.dao

import pl.iterators.kebs.tagged.slick.SlickSupport
import play.api.db.slick.HasDatabaseConfig
import scala.concurrent.Future
import scalaz.{ -\/, \/, \/- }
import helpers.sorus.Fail
import scala.concurrent.ExecutionContext

trait SlickUtils extends SlickSupport {
  self: HasDatabaseConfig[EnhancedPostgresDriver] =>

  protected def option2fail[T](f: Future[Option[T]])(msg: String)(implicit ec: ExecutionContext): Future[Fail \/ T] = {
    f.map(_.map(\/-(_)).getOrElse(-\/(Fail(msg))))
  }
}
