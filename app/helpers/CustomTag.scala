package helpers

import models._
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, Days}
import org.joda.time.Minutes
import org.joda.time.Hours
import play.api.Play
import play.api.Play.current
import play.api.i18n.Lang
import play.twirl.api.Html
import utils.DateUtils
import utils.m
import play.api.mvc.WrappedRequest

class IfTag(condition: Boolean, content: => Html) extends scala.xml.NodeSeq {

  def theSeq = Nil // just ignore, required by NodeSeq

  override def toString = if (condition) content.toString else ""

  def orElse(failed: => Html) = if (condition) content else failed
}

object CustomTag {

  /**
   * Usage :
   *  @isAuthorized(user){
   *    Welcome, @user.name!
   *  }.orElse{
   *    Nothing to see here...
   *  }
   */
  def isAuthorized(user: User)(body: => Html) = new IfTag(user.isAuthorized, body)

  /**
   * format the date to display a delay e.g. 2 days ago or a date if too old
   */
  def date2delay(d: DateTime)(implicit request: WrappedRequest[_]): String = {
    val d_minus_seven_days = DateUtils.now.minusDays(7)
    val d_minus_one_days = DateUtils.now.minusDays(1)
    val d_minus_one_hours = DateUtils.now.minusHours(1)
    val now = DateUtils.now

    if (d.isAfterNow) { "" }
    else if (d.isAfter(d_minus_one_hours)) {
      val minutes_delta = Minutes.minutesBetween(d, now)
      m("general.date.delay.minutes", Math.abs(minutes_delta.getMinutes))
    } else if (d.isAfter(d_minus_one_days)) {
      val hours_delta = Hours.hoursBetween(d, now)
      m("general.date.delay.hours", Math.abs(hours_delta.getHours))
    } else if (d.isAfter(d_minus_seven_days.toInstant)) {
      val day_delta = Days.daysBetween(d, now)
      m("general.date.delay.days", Math.abs(day_delta.getDays))
    } else {
      m("general.date.delay.on", date_format(d, Some("MMM")), d.getDayOfMonth)
    }
  }

  def date_format(date: DateTime, format: Option[String] = None)(implicit lang: Lang): String = {
    val pattern = format
      .orElse(Play.configuration.getString(s"date.i18n.date.format.${lang.language}"))
      .getOrElse("dd/MM/yyyy")
    val locale = lang.toLocale

    val formatter = DateTimeFormat.forPattern(pattern).withLocale(locale)
    formatter.print(date)
  }
}
