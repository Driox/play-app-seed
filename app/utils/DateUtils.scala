package utils

import java.util.Date
import java.text.SimpleDateFormat
import java.util.TimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.{ Days, DateTime, DateTimeZone }
import play.api.libs.json._
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import play.api.data.validation.ValidationError

class String2Date(value: String) {
  def to_date = {
    try { Some(isoFormat.parseDateTime(value)) }
    catch { case e: Exception => None }
  }

  val isoFormat = ISODateTimeFormat.dateTimeNoMillis()
}

object DateUtils {

  //~~~~~~~
  // Json format
  //~~~~~~~~~~~~~~~~~~~~~
  implicit val dateTimeReads: Reads[DateTime] = new Reads[DateTime] {
    val df = org.joda.time.format.ISODateTimeFormat.dateTimeNoMillis()

    def reads(json: JsValue): JsResult[DateTime] = json match {
      case JsNumber(d) => JsSuccess(new DateTime(d.toLong))
      case JsString(s) => parseDate(s) match {
        case Some(d) => JsSuccess(d)
        case None    => JsError(Seq(JsPath() -> Seq(JsonValidationError("validate.error.expected.date.isoformat", "ISO8601"))))
      }
      case _ => JsError(Seq(JsPath() -> Seq(JsonValidationError("validate.error.expected.date"))))
    }

    private def parseDate(input: String): Option[DateTime] = scala.util.control.Exception.allCatch[DateTime] opt (DateTime.parse(input, df))
  }
  implicit val dateTimeWrites: Writes[DateTime] = Writes { (dt: DateTime) => JsString(dateToISO8601(dt)) }
  implicit val jodaDateTimeFormats = Format(dateTimeReads, dateTimeWrites)

  //~~~~~~~
  // Date / DateTime converter
  //~~~~~~~~~~~~~~~~~~~~~
  @inline implicit final def string2Date(ymd: String): String2Date = new String2Date(ymd)
  @inline implicit final def joda2Date(dateTime: DateTime): Date = dateTime.toDate

  //~~~~~~~
  // String / DateTime converter
  //~~~~~~~~~~~~~~~~~~~~~
  def stringToDate(value: String, format: String): DateTime = {
    val formater = DateTimeFormat.forPattern(format)
    formater.parseDateTime(value)
  }

  def dateToString(value: DateTime, format: String): String = {
    val formater = DateTimeFormat.forPattern(format)
    formater.print(value)
  }

  /**
   * iso format : yyyy-MM-ddTHH:mm:ssZ
   * Z can be a timezone, eg +01:00 like in 2015-05-05T14:35:52.657+02:00
   */
  def dateToISO8601(dateTime: DateTime): String = {
    dateTime match {
      case null => ""
      case _ => {
        val fmt: DateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC()
        fmt.print(dateTime)
      }
    }
  }

  def iso8601ToDate(input: String): DateTime = {
    val df = org.joda.time.format.ISODateTimeFormat.dateTimeNoMillis()
    DateTime.parse(input, df)
  }

  def date2remainingDays(value: DateTime): Int = {
    Days.daysBetween(DateTime.now, value).getDays() + 1
  }

  def now(): DateTime = DateTime.now(DateTimeZone.UTC)
}
