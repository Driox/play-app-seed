package helpers.sorus

import play.api.libs.json.{ JsValue, Json, OFormat }

/**
 * Standard error format for Particeep's API
 */
case class Error(technicalCode: String, message: String, code: Option[String] = None, stack: Option[String] = None)

sealed trait ErrorResult extends Product with Serializable
object ErrorResult {
  final case class Errors(hasError: Boolean, errors: List[Error])         extends ErrorResult
  final case class ParsingError(hasError: Boolean, errors: List[JsValue]) extends ErrorResult
}

object Errors {
  implicit val error_format: OFormat[Error]                            = Json.format[Error]
  implicit val errors_format: OFormat[ErrorResult.Errors]              = Json.format[ErrorResult.Errors]
  implicit val parsing_error_format: OFormat[ErrorResult.ParsingError] = Json.format[ErrorResult.ParsingError]

  def format(err: ErrorResult): String = err match {
    case ErrorResult.Errors(_, errors)       => errors.map(_.message).mkString("\n")
    case ErrorResult.ParsingError(_, errors) => errors.map(_.toString()).mkString("\n")
  }
}
