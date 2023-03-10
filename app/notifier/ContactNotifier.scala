package notifier

import akka.actor.ActorSystem
import play.api.Configuration

import javax.inject._
import scala.concurrent.Future
import scala.util.Try

/**
 * @author Grignou
 */
@Singleton
class ContactNotifier @Inject() (val configuration: Configuration, val system: ActorSystem) extends Notifier {

  def notify(firstName: String, lastName: String, email: String, message: String): Future[Try[String]] = {
    val contactMail = "adrien.crovetto@gmail.com" // FROM._1
    sendMail(
      from        = (email, s"$firstName $lastName"),
      to          = Seq(contactMail),
      subject     = "[Reedr]Contact sur Reedr",
      message     = message,
      richMessage = None
    )
  }
}
