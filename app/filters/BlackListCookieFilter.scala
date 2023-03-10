package filters

import akka.stream.Materializer
import play.api.cache.AsyncCacheApi
import play.api.mvc.Results.Redirect
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ ExecutionContext, Future }

/**
 * BlackListCookieFilter is a filter that allow logout to disable session totally
 *
 * This require a link to a storage service so that not decentralized auth anymore but we can't do better right now
 */
@Singleton
class BlackListCookieFilter @Inject() (val mat: Materializer, cache: AsyncCacheApi, ec: ExecutionContext)
  extends Filter {

  def apply(nextFilter: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    isSessionBlacklisted(requestHeader)(ec).flatMap { is_black_listed =>
      if(is_black_listed) {
        Future.successful(Redirect(controllers.routes.Application.home()).withNewSession)
      } else {
        nextFilter(requestHeader)
      }
    }(ec)
  }

  private[this] def isSessionBlacklisted(requestHeader: RequestHeader)(implicit
    ec:                                                 ExecutionContext
  ): Future[Boolean] = {
    requestHeader.session.get("uuid").map { uuid =>
      cache.get(s"BlackListCookieFilter_$uuid").map(_.isDefined)
    }.getOrElse(Future.successful(false))
  }

}
