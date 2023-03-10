package notifier

import akka.actor.ActorSystem
import models.User
import play.api.Configuration
import play.api.i18n.Lang
import utils.m

import javax.inject._
import scala.concurrent.Future
import scala.util.Try

/**
 * @author Grignou
 */
@Singleton
class WelcomeNotifier @Inject() (val configuration: Configuration, val system: ActorSystem) extends Notifier {

  def notify(user: User)(implicit lang: Lang): Future[Try[String]] = {
    sendMail(
      from        = FROM,
      to          = Seq(user.email),
      subject     = m("notifier.welcome.subject"),
      message     = message(user, false),
      richMessage = Some(message(user, true))
    )
  }

  private def message(user: User, html: Boolean)(implicit lang: Lang): String = {
    (lang.language, html) match {
      case ("fr", false) => views.txt.notifier.welcome_fr.render(user, request, lang).toString()
      case ("fr", true)  => views.html.notifier.welcome_fr.render(user, request, lang).toString()
      case ("en", false) => views.txt.notifier.welcome_en.render(user, request, lang).toString()
      case ("en", true)  => views.html.notifier.welcome_en.render(user, request, lang).toString()
      case _             => views.html.notifier.welcome_en.render(user, request, lang).toString()
    }
  }
}
