package filters

import javax.inject.Inject
import akka.stream.Materializer
import scala.concurrent.Future
import play.api.mvc._
import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * @author Grignou
 *
 * set in application.conf
 * <p>
 *   trustxforwarded=true
 *
 *   trust header from a frontal on another machine
 *   cf. https://www.playframework.com/documentation/2.3.x/HTTPServer#Advanced-proxy-settings
 * </p>
 * <p>api.secure=true</p>
 *
 */
class HttpsFilter @Inject() (implicit val mat: Materializer) extends Filter {

  def apply(nextFilter: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    val isEnable = Play.configuration.getBoolean("application.is.secure").getOrElse(true)

    if (!isEnable) {
      nextFilter(requestHeader)
    } else {
      requestHeader.secure match {
        case true  => nextFilter(requestHeader).map(_.withHeaders("Strict-Transport-Security" -> "max-age=31536000; includeSubDomains"))
        case false => redirectToHttps(requestHeader)
      }
    }
  }

  private def redirectToHttps(requestHeader: RequestHeader) = {
    Future.successful(Results.MovedPermanently("https://" + requestHeader.host + requestHeader.uri))
  }
}
