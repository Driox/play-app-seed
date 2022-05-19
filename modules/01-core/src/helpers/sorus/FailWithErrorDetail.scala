package helpers.sorus

import play.api.Logging
import play.api.i18n.Lang
import play.api.mvc.Results._
import play.api.mvc._
import scalaz.{ -\/, \/, \/- }

case class FailWithErrorDetail(
  override val message: String,
  override val cause:   Option[Throwable \/ Fail] = None,
  val errors:           ErrorResult
) extends Fail(message, cause)
    with Logging {

  def userMessage()(implicit lng: Lang): String = ErrorHandler.toUserMessage(errors)

  def withResult(result: Result)(implicit lng: Lang): Result = {
    logger.error(ErrorHandler.toLog(errors))
    result match {
      case redirect if(result.header.status >= 300 && result.header.status < 400) => {
        redirect.flashing("error" -> userMessage())
      }
      case _                                                                      => (new Status(result.header.status))(userMessage())
    }
  }

  override def withEx(s: String): Fail = new FailWithErrorDetail(s, Some(\/-(this)), errors)

  override def withEx(ex: Throwable): Fail = new FailWithErrorDetail(this.message, Some(-\/(ex)), errors)

  override def withEx(fail: Fail): Fail = new FailWithErrorDetail(this.message, Some(\/-(fail)), errors)
}
