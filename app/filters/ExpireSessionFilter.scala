package filters

import scala.concurrent.Future
import play.api.Configuration
import play.api.mvc._
import play.api.mvc.Results.Redirect
import javax.inject._

import akka.stream.Materializer
import utils.TimeUtils
import scala.util.Try

/**
 * This filter redirect to the login if the session as expired
 *
 * negative value of play.application.inactivity.seconds_allowed disable the filter
 */
@Singleton
class ExpireSessionFilter @Inject() (val mat: Materializer, config: Configuration) extends Filter {

  def apply(nextFilter: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    if(isEnable(requestHeader) && isTokenExpired(requestHeader)) {
      Future.successful(Redirect(controllers.routes.Application.home()).withNewSession)
    } else {
      nextFilter(requestHeader)
    }
  }

  // we filter only if expire_at is present in the token
  private[this] def isTokenExpired(requestHeader: RequestHeader): Boolean = {
    (for {
      expire_at <- requestHeader.session.get("expire_at")
    } yield {
      Try {
        expire_at.toLong < timestamp()
      }.getOrElse(true)
    }).getOrElse(false)
  }

  private[this] def timestamp(): Long = TimeUtils.now.toEpochSecond()

  private[this] val apiPath = "^/v[0-9]*/.*"
  private[this] def isEnable(header: RequestHeader): Boolean = !header.path.matches(apiPath)
}
