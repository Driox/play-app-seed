package event.infrastructure.pulsar

import akka.actor.ActorSystem
import play.api.{ Configuration, Logging }

import scala.concurrent.ExecutionContext

import org.apache.pulsar.client.api.AuthenticationFactory

import com.sksamuel.pulsar4s._

private[pulsar] class PulsarApplicationClient(config: Configuration, system: ActorSystem) extends Logging {

  logger.info("--- Module started : Pulsar Client ---")

  val ec: ExecutionContext = system.dispatchers.lookup("contexts.pulsar-publisher")

  private[this] val url          = config.get[String]("pulsar.url")
  private[this] val token        = config.get[String]("pulsar.token")
  private[this] val tenant       = config.get[String]("pulsar.tenant")
  private[this] val ns           = config.get[String]("pulsar.ns")
  private[this] val environment  = config.get[String]("application.environment")
  private[this] val topic_prefix = environment

  private[this] val pulsar_config   = PulsarClientConfig(
    serviceUrl     = url,
    authentication = Some(AuthenticationFactory.token(token))
  )
  private[pulsar] val pulsar_client = PulsarClient(pulsar_config)

  private[pulsar] def build_topic(topic_name: String)       = Topic(s"persistent://$tenant/$ns/$topic_prefix-$topic_name")
  private[pulsar] def build_topic_regex(topic_name: String) = s"$tenant/$ns/$topic_prefix-$topic_name".r

  def default_properties(): Map[String, String] = Map(
    "application" -> core.build.BuildInfo.name,
    "version"     -> core.build.BuildInfo.version,
    "environment" -> environment
  )

  def onStop(): Unit = {
    pulsar_client.close()
    logger.info("--- Module stopped : Pulsar Client ---")
  }
}
