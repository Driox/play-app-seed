package controllers

import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{ FormError, Mapping }
import play.api.mvc.RequestHeader

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

import scala.util.control.NonFatal

trait PlayHelper {

  /**
   * Best practice to always use ISO format
   */
  val offsetDate: Mapping[OffsetDateTime] = of(offsetDateFormat())

  def offsetDateFormat(pattern: DateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME): Formatter[OffsetDateTime] =
    new Formatter[OffsetDateTime] {
      def bind(key: String, data: Map[String, String]) = {
        try {
          val date_to_parse = data.getOrElse(key, "")
          val date          = OffsetDateTime.parse(date_to_parse, pattern)
          Right(date)
        } catch {
          case NonFatal(_) => Left(List(FormError(key, "error.zoned.date")))
        }
      }

      def unbind(key: String, value: OffsetDateTime) = Map(key -> value.format(pattern))
    }

  def is_ajax(requestHeader: RequestHeader): Boolean =
    requestHeader.headers.get("X-Requested-With").map(_.startsWith("play-zio-template")).getOrElse(false)
}
object PlayHelper extends PlayHelper
