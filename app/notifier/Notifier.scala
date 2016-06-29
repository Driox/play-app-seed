package notifier

import play.api.mvc.RequestHeader
import play.libs.Akka
import scala.concurrent.{ExecutionContext, Future}
import org.apache.commons.mail._
import play.api.Play
import play.api.Play.current
import scala.util.Try
import play.api.Logger

trait Notifier {

  protected val FROM = (Play.configuration.getString("mail.contact.email").getOrElse("do.not.reply@mon-app.com"), "Contact team")
  implicit protected val request: RequestHeader = new RequestHeader() {
    override lazy val host = Play.configuration.getString("application.baseurl").getOrElse("http://mon-app.com")
    def remoteAddress = ???
    def headers = ???
    def queryString = ???
    def version = ???
    def method = ???
    def path = ???
    def uri = ???
    def tags = ???
    def id = ???
    def secure = false
    def clientCertificateChain = ???
  }

  private val SMTP_HOST = Play.configuration.getString("mail.smtp.host").getOrElse("mock")
  private val SMTP_PORT = Play.configuration.getInt("mail.smtp.port").getOrElse(80)
  private val SMTP_USER = Play.configuration.getString("mail.smtp.user").getOrElse("")
  private val SMTP_PASSWORD = Play.configuration.getString("mail.smtp.password").getOrElse("")
  private val SMTP_TRANSACTION_IP = Play.configuration.getString("mail.smtp.ip.transaction").getOrElse("127.0.0.1")
  private val mail_execution_context: ExecutionContext = Akka.system.dispatchers.lookup("contexts.mail_execution_context")

  /**
   *  Sends an email
   *  @return Whether sending the email was a success
   */
  def sendMail(
    from:        (String, String),
    to:          Seq[String],
    cc:          Seq[String]      = Seq.empty,
    bcc:         Seq[String]      = Seq.empty,
    subject:     String,
    message:     String,
    richMessage: Option[String]   = None
  ): Future[Try[String]] = {
    SMTP_HOST match {
      case "mock" => sendMockMail(from, to, cc, bcc, subject, message, richMessage)
      case _      => sendRealMail(from, to, cc, bcc, subject, message, richMessage)
    }
  }

  private def sendRealMail(
    from:        (String, String),
    to:          Seq[String],
    cc:          Seq[String]      = Seq.empty,
    bcc:         Seq[String]      = Seq.empty,
    subject:     String,
    message:     String,
    richMessage: Option[String]   = None
  ) = Future {

    val commonsMail: Email = richMessage.isDefined match {
      case true  => new HtmlEmail().setHtmlMsg(richMessage.get).setTextMsg(message)
      case false => new SimpleEmail().setMsg(message)
    }

    setUpConfig(commonsMail)

    to.foreach(commonsMail.addTo(_))
    cc.foreach(commonsMail.addCc(_))
    bcc.foreach(commonsMail.addBcc(_))
    val preparedMail = commonsMail.
      setFrom(from._1, from._2).
      setSubject(subject)

    // Send the email and check for exceptions
    Try(preparedMail.send)
  }(mail_execution_context)

  private def setUpConfig(mail: Email) {
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
    cc:          Seq[String]      = Seq.empty,
    bcc:         Seq[String]      = Seq.empty,
    subject:     String,
    message:     String,
    richMessage: Option[String]   = None
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
    Logger.info(mailing)
    Future.successful(Try(mailing))
  }
}
