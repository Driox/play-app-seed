package event.infrastructure.pulsar

import akka.actor.ActorSystem
import helpers.sorus.SorusDSL._
import play.api.Configuration
import play.api.libs.json._
import play.api.libs.ws.{ EmptyBody, WSClient }

import javax.inject.{ Inject, Singleton }
import scala.concurrent.ExecutionContext

import org.apache.pulsar.client.admin.{ PulsarAdmin => PulsarAdminJava }
import org.apache.pulsar.client.api.AuthenticationFactory
import org.apache.pulsar.client.admin

@Singleton
class PulsarAdmin @Inject() (
  config:      Configuration,
  system:      ActorSystem,
  ws:          WSClient
)(implicit ec: ExecutionContext) extends Sorus {

  private[this] val url         = config.get[String]("pulsar.admin.url")
  private[this] val token       = config.get[String]("pulsar.token")
  private[this] val tenant      = config.get[String]("pulsar.tenant")
  private[this] val ns          = config.get[String]("pulsar.ns")
  private[this] val environment = config.get[String]("application.environment")

  lazy val client: PulsarAdminJava = buildClient()

  private[this] val topic_base_url = s"$url/admin/v2/persistent/${tenant}/${ns}"

  private[this] def buildClient() = {
    PulsarAdminJava
      .builder()
      .serviceHttpUrl(url)
      .authentication(AuthenticationFactory.token(token))
      .build()
  }

  def setDeduplicationEnablAllTopic(): Step[String]       = {
    ws
      .url(s"$url/admin/v2/namespaces/${tenant}/${ns}/deduplication")
      .addHttpHeaders("Authorization" -> s"Bearer $token")
      .post(JsTrue)
      .map(_.body) ?| "Failure"
  }
  def setDeduplicationEnable(topic: String): Step[String] = {
    ws
      .url(s"$topic_base_url/$topic/deduplicationEnabled")
      .addHttpHeaders("Authorization" -> s"Bearer $token")
      .post(EmptyBody)
      .map(_.body) ?| "Failure"
  }
  def getDeduplicationEnable(topic: String): Step[String] = {
    ws
      .url(s"$topic_base_url/$topic/deduplicationEnabled?applied=true")
      .addHttpHeaders("Authorization" -> s"Bearer $token")
      .get()
      .map(_.body) ?| "Failure"
  }

  def all_topic(like: String): Step[List[String]] = {
    ws
      .url(topic_base_url)
      .addHttpHeaders("Authorization" -> s"Bearer $token")
      .withQueryStringParameters("includeSystemTopic" -> "false")
      .get()
      .map(_.body)
      .map(str => Json.parse(str).asOpt[List[String]].getOrElse(List()))
      .map(_.filter(str => str.contains(s"$environment-")))
      .map(_.filter(str => str.contains(like))) ?| "Failure"
  }
}
