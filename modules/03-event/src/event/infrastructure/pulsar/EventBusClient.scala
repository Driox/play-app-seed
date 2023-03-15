package event.infrastructure.pulsar

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import event._
import helpers.sorus.Fail
import play.api.Configuration
import play.api.libs.json._
import scalaz.\/

import javax.inject.{ Inject, Singleton }
import scala.concurrent.Future
import scala.concurrent.duration._

import org.apache.pulsar.client.api.{ CompressionType, SubscriptionInitialPosition, SubscriptionType }

import com.sksamuel.pulsar4s._
import com.sksamuel.pulsar4s.akka.streams._

@Singleton
class EventBusClient @Inject() (
  config: Configuration,
  system: ActorSystem
) extends EventBusPublisher
    with EventBusListener {

  private[this] val pulsar_app                        = new PulsarApplicationClient(config, system)
  private[this] val pulsar_listener: PulsarListener   = new PulsarListener(pulsar_app)
  private[this] val pulsar_publisher: PulsarPublisher = new PulsarPublisher(pulsar_app)

  // for logging purpose
  def topic_name(s: String): Topic = pulsar_app.build_topic(s)

  def publish[EVENT_BODY](topic_name: String, event: Event[EVENT_BODY]): Future[Fail \/ MessageId] = {
    val producer_config = build_producer_config(topic_name)
    pulsar_publisher.publish(event, producer_config)
  }

  /**
   * We need a productName on producer to have optimistic locking available
   * Pulsar ensure only one producer can publish at the same time
   * Associate with event deduplication this ensure a good event flow
   */
  private[this] def build_producer_config(topic_name: String): ProducerConfig = {
    val topic = pulsar_app.build_topic(topic_name)
    ProducerConfig(
      topic                   = topic,
      sendTimeout             = Some(0 second),
      enableBatching          = Some(true),
      batchingMaxMessages     = Some(1000),
      batchingMaxPublishDelay = Some(100 milliseconds),
      compressionType         = Some(CompressionType.ZLIB)
    )
  }

  /**
    * If subscription_name is a constant then the subscription (i.e. stream of events) would be unique amongs every server's instance
    * This is usefull for jobs, sending mail, etc...
    *
    * If subscription_name is random (like s"all-user-${uuid}") then we can have many streams of events
    *
    * If you want more control / usecase then you need to expose ConsumerConfig as an input
    * see doc https://pulsar.apache.org/docs/2.11.x/concepts-messaging/#subscriptions
    */
  def subscribe(
    subscription_name: String,
    topic_name:        String,
    criteria:          EventSearchCriteria = EventSearchCriteria()
  ): Source[Fail \/ Event[JsValue], Control] = {
    val consumer_config = build_consumer_config(topic_name, Some(Subscription(subscription_name)))
    pulsar_listener.subscribe(consumer_config, criteria)
  }

  private[this] def build_consumer_config(
    topic_name:        String,
    subscription_name: Option[Subscription]
  ): ConsumerConfig = {
    val (topics, topic_pattern) = topic_name match {
      case s if s.contains("*") => (Nil, Some(pulsar_app.build_topic_regex(topic_name)))
      case _                    => (Seq(pulsar_app.build_topic(topic_name)), None)
    }

    ConsumerConfig(
      subscriptionName            = subscription_name.getOrElse(Subscription.generate),
      topics                      = topics,
      topicPattern                = topic_pattern,
      subscriptionType            = Some(SubscriptionType.Shared),
      subscriptionInitialPosition = subscription_name
        .map(_ => SubscriptionInitialPosition.Earliest)
        .orElse(Some(SubscriptionInitialPosition.Latest))
    )
  }

  def onStop(): Unit = pulsar_app.onStop()

}
