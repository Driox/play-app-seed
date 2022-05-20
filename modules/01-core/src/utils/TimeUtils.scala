package utils

import play.api.libs.json._

import java.time.format.DateTimeFormatter
import java.time.{ Instant, Month, OffsetDateTime, ZoneId, ZoneOffset }
import java.util.Locale

import scala.util.Try

import com.ibm.icu.text.DateTimePatternGenerator

object TimeUtils {

  private[this] val pattern: DateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME.withZone(ZoneOffset.UTC)

  def from_millis(milliseconds: Long): OffsetDateTime =
    OffsetDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), ZoneId.of("UTC"))

  def from(year: Int, month: Month, day: Int): OffsetDateTime = {
    start_of_today().withYear(year).withMonth(month.getValue).withDayOfMonth(day)
  }

  def isIso(date: String): Boolean = parse(date).isDefined

  def parse(date: String): Option[OffsetDateTime] = Try {
    Some(OffsetDateTime.parse(date, pattern))
  }.getOrElse(None)

  def toIso(date: OffsetDateTime): String = pattern.format(date.withNano(0))

  def toString(date: OffsetDateTime, dateFormat: String): String = {
    val dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat)
    date.format(dateTimeFormatter)
  }

  def now: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC).withNano(0)

  def start_of_today(): OffsetDateTime = now.withHour(0).withMinute(0).withSecond(0).withNano(0)

  def now_iso(): String = toIso(now)

  def now_day(): String = {
    val date = now
    s"${date.getYear}-${date.getMonth.getValue}-${date.getDayOfMonth}"
  }

  def format(date: OffsetDateTime, format: String): String = {
    DateTimeFormatter.ofPattern(format).format(date)
  }

  def format(date: OffsetDateTime, locale: Locale): String = {
    val pattern = DateTimePatternGenerator.getInstance(locale).getBestPattern("dd/MM/yyyy")
    format(date, pattern)
  }

  def best_pattern(locale: Locale): String = DateTimePatternGenerator.getInstance(locale).getBestPattern("dd/MM/yyyy")

  val format: Format[OffsetDateTime] = new Format[OffsetDateTime] {
    def reads(json: JsValue): JsResult[OffsetDateTime] = json match {
      case JsString(s) => parse(s).map(JsSuccess(_)).getOrElse(JsError(s"can't parse $s as ISO date"))
      case _           => JsError(s"can't parse $json as ISO date")
    }
    def writes(t:   OffsetDateTime): JsValue           = JsString(toIso(t))
  }
}
