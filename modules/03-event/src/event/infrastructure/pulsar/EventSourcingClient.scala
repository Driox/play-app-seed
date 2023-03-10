package event.infrastructure.pulsar

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import event.services._
import event.{ Event, EventSearchCriteria }
import helpers.sorus.Fail
import play.api.Configuration
import play.api.libs.json._
import scalaz.{ -\/, \/, \/- }
import tagged.Tags.Id

import javax.inject.{ Inject, Singleton }
import scala.concurrent.Future
import scala.concurrent.duration._

import org.apache.pulsar.client.api.{ CompressionType, SubscriptionInitialPosition, SubscriptionType }

import com.sksamuel.pulsar4s._
import com.sksamuel.pulsar4s.akka.streams._

@Singleton
class EventSourcingClient @Inject() (
  config: Configuration,
  system: ActorSystem
) extends EventSourcing {

  private[this] val pulsar_app                        = new PulsarApplicationClient(config, system)
  private[this] val pulsar_listener: PulsarListener   = new PulsarListener(pulsar_app)
  private[this] val pulsar_publisher: PulsarPublisher = new PulsarPublisher(pulsar_app)

  def persist[EVENT_BODY](event: Event[EVENT_BODY]): Future[Fail \/ MessageId] = {
    val topic_name      = compute_topic_name(event.entity_type, event.entity_id)
    val producer_config = build_producer_config(topic_name)
    pulsar_publisher.publish(event, producer_config)
  }

  private[this] def compute_topic_name(entity_type: String, entity_id: String) =
    s"event-sourced-${entity_type}-${entity_id}"

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

  def reload_event[EntityType](
    entity_type:   String,
    entity_id:     Id[EntityType],
    criteria:      EventSearchCriteria = EventSearchCriteria()
  )(implicit read: Reads[EntityType]): Source[Fail \/ Event[EntityType], Control] = {
    val topic_name      = compute_topic_name(entity_type, entity_id)
    val consumer_config = build_consumer_config(topic_name, None)
    pulsar_listener
      .subscribe(consumer_config, criteria)
      .map(_.flatMap(entity => parseJsonToEvent(entity)))
  }

  private[this] def parseJsonToEvent[EntityType](event_json: Event[JsValue])(implicit
    read:                                                    Reads[EntityType]
  ): Fail \/ Event[EntityType] = {
    event_json.payload.validate[EntityType] match {
      case JsSuccess(entity, _) => \/-(event_json.copy[EntityType](payload = entity))
      case JsError(err)         => -\/(Fail(err))
    }
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
