package notifier

import akka.actor.ActorSystem
import play.api.mvc.RequestHeader
import play.api.mvc.request.RemoteConnection
import play.api.{ Configuration, Logging }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

import org.apache.commons.mail._

trait Notifier extends Logging {

  def configuration: Configuration
  def system: ActorSystem

  protected val FROM                            = (configuration.get[String]("mail.contact.email"), "Contact team")
  implicit protected val request: RequestHeader = new RequestHeader() {
    override lazy val host = configuration.get[String]("application.baseurl")
    def headers            = ???
    def version            = ???
    def method             = ???
    def attrs              = ???
    def connection         = RemoteConnection("127.0.0.1", true, None)
    def target             = ???
  }

  private val SMTP_HOST                                = configuration.get[String]("mail.smtp.host")
  private val SMTP_PORT                                = configuration.get[Int]("mail.smtp.port")
  private val SMTP_USER                                = configuration.get[String]("mail.smtp.user")
  private val SMTP_PASSWORD                            = configuration.get[String]("mail.smtp.password")
  private val SMTP_TRANSACTION_IP                      = configuration.get[String]("mail.smtp.ip.transaction")
  private val mail_execution_context: ExecutionContext = system.dispatchers.lookup("contexts.mail_execution_context")

  /**
   *  Sends an email
   *  @return Whether sending the email was a success
   */
  def sendMail(
    from:        (String, String),
    to:          Seq[String],
    cc:          Seq[String]    = Seq.empty,
    bcc:         Seq[String]    = Seq.empty,
    subject:     String,
    message:     String,
    richMessage: Option[String] = None
  ): Future[Try[String]] = {
    SMTP_HOST match {
      case "mock" => sendMockMail(from, to, cc, bcc, subject, message, richMessage)
      case _      => sendRealMail(from, to, cc, bcc, subject, message, richMessage)
    }
  }

  private def sendRealMail(
    from:        (String, String),
    to:          Seq[String],
    cc:          Seq[String]    = Seq.empty,
    bcc:         Seq[String]    = Seq.empty,
    subject:     String,
    message:     String,
    richMessage: Option[String] = None
  ) = Future {

    val commonsMail: Email = richMessage.isDefined match {
      case true  => new HtmlEmail().setHtmlMsg(richMessage.get).setTextMsg(message)
      case false => new SimpleEmail().setMsg(message)
    }

    setUpConfig(commonsMail)

    to.foreach(commonsMail.addTo(_))
    cc.foreach(commonsMail.addCc(_))
    bcc.foreach(commonsMail.addBcc(_))
    val preparedMail = commonsMail.setFrom(from._1, from._2).setSubject(subject)

    // Send the email and check for exceptions
    Try(preparedMail.send)
  }(mail_execution_context)

  private def setUpConfig(mail: Email) = {
    mail.setHostName(SMTP_HOST)
    mail.setSmtpPort(SMTP_PORT)
    mail.setSSLOnConnect(true)
    mail.setAuthentication(SMTP_USER, SMTP_PASSWORD)

    mail.addHeader("X-Mailin-IP", SMTP_TRANSACTION_IP)
  }

  /**
   *  Sends an email
   *  @return Whether sending the email was a success
   */
  private def sendMockMail(
    from:        (String, String),
    to:          Seq[String],
    cc:          Seq[String]    = Seq.empty,
    bcc:         Seq[String]    = Seq.empty,
    subject:     String,
    message:     String,
    richMessage: Option[String] = None
  ) = {

    val mailing = s"""
--- Sending email ---

From : ${from._1} - ${from._2}
To   : ${to.mkString(",")}
Cc   : ${cc.mkString(",")}
Bcc  : ${bcc.mkString(",")}

Subject : $subject

$message

$richMessage
"""
    logger.info(mailing)
    Future.successful(Try(mailing))
  }
}
