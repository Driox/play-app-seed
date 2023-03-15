package event.infrastructure.pulsar

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import event._
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
   * We need a producerName on producer to have optimistic locking available
   * Pulsar ensure only one producer can publish at the same time
   * Associate with event deduplication this ensure a good event flow
   *
   * We close producer after every message so we may have multiple instance of the server
   * and each instance publish message
   * Pulsar enforce a SingleWriter at the same time
   *
   * see : https://github.com/apache/pulsar/wiki/PIP-68:-Exclusive-Producer
   */
  private[this] def build_producer_config(topic_name: String): ProducerConfig = {
    val topic = pulsar_app.build_topic(topic_name)
    ProducerConfig(
      topic                = topic,
      sendTimeout          = Some(0 second), // see https://pulsar.apache.org/docs/2.11.x/cookbooks-deduplication/#pulsar-clients
      enableBatching       = Some(false),    // no batching, we want event as unit message
      compressionType      = Some(CompressionType.ZLIB),
      producerName         = Some(topic_name),
      additionalProperties = Map(
        // XXX : see https://github.com/apache/pulsar/tree/82237d3684fe506bcb6426b3b23f413422e6e4fb/pulsar-client/src/main/java/org/apache/pulsar/client/impl/conf
        "accessMode" -> "WaitForExclusive"
      )
    )
  }

  def reload_event[EntityIdType, EventPayload](
    entity_type:   String,
    entity_id:     Id[EntityIdType],
    criteria:      EventSearchCriteria = EventSearchCriteria()
  )(implicit read: Reads[EventPayload]): Source[Fail \/ Event[EventPayload], Control] = {
    val topic_name      = compute_topic_name(entity_type, entity_id)
    val consumer_config = build_consumer_config(topic_name)
    pulsar_listener
      .subscribe(consumer_config, criteria)
      .map(_.flatMap(entity => parseJsonToEvent(entity)))
  }

  private[this] def parseJsonToEvent[EventPayload](event_json: Event[JsValue])(implicit
    read:                                                      Reads[EventPayload]
  ): Fail \/ Event[EventPayload] = {
    event_json.payload.validate[EventPayload] match {
      case JsSuccess(entity, _) => \/-(event_json.copy[EventPayload](payload = entity))
      case JsError(err)         => -\/(Fail(err))
    }
  }

  private[this] def build_consumer_config(topic_name: String): ConsumerConfig = {
    val (topics, topic_pattern) = topic_name match {
      case s if s.contains("*") => (Nil, Some(pulsar_app.build_topic_regex(topic_name)))
      case _                    => (Seq(pulsar_app.build_topic(topic_name)), None)
    }

    ConsumerConfig(
      subscriptionName            = Subscription.generate,
      topics                      = topics,
      topicPattern                = topic_pattern,
      subscriptionType            = Some(SubscriptionType.Exclusive), // we want all the event in one subscription
      subscriptionInitialPosition = Some(SubscriptionInitialPosition.Earliest)
    )
  }

  def onStop(): Unit = pulsar_app.onStop()

}
