package effect.zio.play

import effect.Fail
import play.api.Logging
import play.api.data.Form
import play.api.libs.json.{ JsPath, JsValue, Json, JsonValidationError, Reads }
import play.api.mvc.Result
import utils.StringUtils

import zio.{ IO, ZIO }

case class FailWithResult(
  override val message: String,
  val result:           Result,
  override val cause:   Option[Either[Throwable, Fail]] = None
) extends Fail(message, cause) {
  override def withEx(fail: Fail): FailWithResult = new FailWithResult(this.message, result, Some(Right(fail)))
}

/**
 * Convert json validator / Form / etc... to Fail in ZIO effect
 */
object ZioPlayHelper extends ZioPlayHelper
trait ZioPlayHelper  extends Logging {

  type JsErrorContent = scala.collection.Seq[(JsPath, scala.collection.Seq[JsonValidationError])]

  def defaultJsonError2Fail(err: JsErrorContent): Fail = {
    val msg = err.map { case (path, errors) =>
      val entry   = path.toString()
      val err_msg = errors.map(_.messages.mkString(", ")).mkString(", ")

      s"$entry : $err_msg"
    }.mkString(", ")

    new Fail(msg)
  }

  protected def jsonValidation[A](jsValue: JsValue)(implicit reads: Reads[A]): ZIO[Any, Fail, A] =
    IO.fromEither(jsValue.validate[A].asEither).mapError(defaultJsonError2Fail)

  implicit def form2Zio[A](form: Form[A]): ZioForm[A] = new ZioForm(form)

  /**
   * Allow this kind of mapping with result on the left
   *
   * criteria <- eventSearchForm.bindFromRequest ?| (formWithErrors => Ok(views.html.analyzer.index(formWithErrors)))
   */
  implicit def result2Fail(result: Result): FailWithResult = {
    FailWithResult("result from ctrl", result)
  }

  def fail2json(fail: Fail): JsValue = {
    val code = StringUtils.randomAlphanumericString(8)
    fail.getRootException()
      .map(t => logger.error(s"[#$code]${fail.userMessage()}", t))
      .getOrElse(logger.error(s"[#$code]${fail.userMessage()}"))

    Json.obj(
      "has_error" -> true,
      "code"      -> s"#$code",
      "message"   -> fail.message
    )
  }
}

class ZioForm[T](form: Form[T]) {
  def ?|(failureHandler: (Form[T]) => Fail): ZIO[Any, Fail, T] = {
    ZIO.fromEither(
      form.fold(failureHandler andThen Left.apply, Right.apply)
    )
  }

  def ?|(unit: Unit): ZIO[Any, Fail, T]                                       = ?|(f => default_failure_handler(f))
  private[this] def default_failure_handler[A](formWithErrors: Form[A]): Fail = {
    val msg = formWithErrors.errors.map(_.message).mkString("\n")
    Fail(s"Error in your input data : $msg")
  }
}
