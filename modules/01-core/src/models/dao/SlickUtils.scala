package models.dao

import helpers.sorus.Fail
import pl.iterators.kebs.tagged.slick.SlickSupport
import play.api.db.slick.HasDatabaseConfig
import scalaz.{ -\/, \/, \/- }

import scala.concurrent.{ExecutionContext, Future}

trait SlickUtils extends SlickSupport {
  self: HasDatabaseConfig[EnhancedPostgresDriver] =>

  protected def option2fail[T](f: Future[Option[T]])(msg: String)(implicit ec: ExecutionContext): Future[Fail \/ T] = {
    f.map(_.map(\/-(_)).getOrElse(-\/(Fail(msg))))
  }
}
