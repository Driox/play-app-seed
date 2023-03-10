package helpers

import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{ FormError, Mapping }
import play.api.i18n.{ Lang, Langs }
import play.api.mvc.RequestHeader

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import scala.util.control.NonFatal

object PlayHelper {

  /**
   * Best practice to always use ISO format
   */
  val zonedDate: Mapping[OffsetDateTime] = of(zonedDateFormat())

  def zonedDateFormat(pattern: DateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME): Formatter[OffsetDateTime] =
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
    requestHeader.headers.get("X-Requested-With").map(_.startsWith("particeep-plug-life")).getOrElse(false)

  def defaultLang(langs: Langs): Lang = langs.availables.headOption.getOrElse(Lang("fr"))

}
