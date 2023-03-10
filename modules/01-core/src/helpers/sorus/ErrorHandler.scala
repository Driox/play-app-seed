package helpers.sorus

import play.api.i18n.Lang
import play.api.libs.json.{ JsObject, JsValue }
import utils.m

object ErrorHandler {

  private[this] def json_msg(err: ErrorResult.ParsingError)(implicit lng: Lang): String =
    m("error.parsing.title") + " : " + err.errors.map(parse_json_error).mkString("\n")

  private[this] def parse_json_error(err: JsValue)(implicit lng: Lang): String = {
    err match {
      case j: JsObject => parse_json_error_object(j)
      case j           => j.toString()
    }
  }

  private[this] def parse_json_error_object(obj: JsObject)(implicit lng: Lang): String = {
    obj.fields.map(f => m("error.parsing.field", f._1) + " : " + f._2.toString()).mkString("\n")
  }

  private[this] def code(err: Error): String = err.code.map("[" + _ + "] ").getOrElse("")

  private[this] def top_level_message(err: Error)(implicit lng: Lang): String =
    code(err) + " " + translate(err.technicalCode)

  private[this] def second_level_message(err: Error)(implicit lng: Lang): String = {
    top_level_message(err) + " : " + err.message + " - " + err.stack.getOrElse("no stack available")
  }

  private[this] val i18n_regex                                       = "^([a-z0-9\\.]*)$".r
  private[this] def translate(s: String)(implicit lng: Lang): String = {
    s match {
      case i18n_regex(s) => m(s)
      case _             => s
    }
  }

  def toLog(error: ErrorResult): String = {
    implicit val lng: Lang = Lang("en")
    error match {
      case api_error: ErrorResult.Errors        => api_error.errors.map(second_level_message).mkString("\n")
      case json_error: ErrorResult.ParsingError => json_msg(json_error)
    }
  }

  def toUserMessage(error: ErrorResult)(implicit lng: Lang): String = {
    error match {
      case api_error: ErrorResult.Errors        => api_error.errors.map(top_level_message).mkString("\n")
      case json_error: ErrorResult.ParsingError => json_msg(json_error)
    }
  }

  def errorResult2Fail(err: ErrorResult): Fail = {
    FailWithErrorDetail(
      message = "Api Error : " + Errors.format(err),
      errors  = err
    )
  }

}
