package notifier

import models.User
import play.api.mvc.RequestHeader
import scala.util.Try
import scala.concurrent.Future

import play.api.{Logger, Play, Application}
import play.api.Play.current
import play.api.i18n.Lang
import utils.m

/**
 * @author Grignou
 */
object WelcomeNotifier extends Notifier {

  def notify(user: User)(implicit app: Application, lang: Lang): Future[Try[String]] = {
    sendMail(
      from = FROM,
      to = Seq(user.email),
      subject = m("notifier.welcome.subject"),
      message = message(user, false),
      richMessage = Some(message(user, true))
    )
  }

  private def message(user: User, html: Boolean)(implicit lang: Lang): String = {
    (lang.language, html) match {
      case ("fr", false) => views.txt.notifier.welcome_fr.render(user, request, lang).toString()
      case ("fr", true)  => views.html.notifier.welcome_fr.render(user, request, lang).toString()
      case ("en", false) => views.txt.notifier.welcome_en.render(user, request, lang).toString()
      case ("en", true)  => views.html.notifier.welcome_en.render(user, request, lang).toString()
    }
  }
}
