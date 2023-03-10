package event.infrastructure.pulsar

import akka.stream.scaladsl.Source
import event._
import helpers.DisjunctionHelper
import helpers.sorus.Fail
import play.api.libs.json._
import scalaz.{ -\/, \/, \/- }

import org.apache.pulsar.client.api.Schema

import com.sksamuel.pulsar4s._
import com.sksamuel.pulsar4s.akka.streams._
import com.sksamuel.pulsar4s.playjson._

private[pulsar] class PulsarListener(pulsar_app: PulsarApplicationClient) extends DisjunctionHelper {

  /**
   * NB: every message in the stream is acknowledge by the pulsar4s lib
   */
  private[pulsar] def subscribe(
    consumer_config: ConsumerConfig,
    criteria:        EventSearchCriteria
  ): Source[Fail \/ Event[JsValue], Control] = {
    event_source(consumer_config)
      .filter { event =>
        criteria.publish_after.forall(_ < event.eventTime.value) &&
        criteria.sequence_nb_after.forall(_ < event.sequenceId.value)
      }
      .map(msg => parse_message(msg))
      .filter {
        case -\/(_)     => true
        case \/-(event) =>
          criteria.event_name.forall(_ == event.name) &&
          criteria.created_by.forall(_ == event.created_by)
      }
  }

  private[this] def event_source(consumer_config: ConsumerConfig): Source[ConsumerMessage[JsValue], Control] = {
    val consumerFn = () => build_consumer[JsValue](consumer_config)
    source(consumerFn)
  }

  private[this] def build_consumer[T](consumer_config: ConsumerConfig)(implicit schema: Schema[T]): Consumer[T] = {
    pulsar_app.pulsar_client.consumer[T](consumer_config)
  }

  private[this] def parse_message(message: ConsumerMessage[JsValue]): Fail \/ Event[JsValue] = {
    for {
      event_id         <- message.props.get("id")          |> "Pulsar parsing error : no event id"
      entity_type      <- message.props.get("entity_type") |> "Pulsar parsing error : no event entity_type"
      entity_id        <- message.key                      |> "Pulsar parsing error : no event key"
      event_name_props <- message.props.get("event_name")  |> "Pulsar parsing error : no event name"
      event_name       <- EventName.parse(
                            event_name_props
                          ) |> s"Pulsar parsing error : Error parsing event name from key $event_name_props"
      author           <- message.props.get("created_by")  |> "Pulsar parsing error : no created by"
    } yield {
      Event(
        id          = EventId(event_id),
        name        = event_name,
        sequence_nb = message.sequenceId.value,
        created_at  = Timestamp(message.eventTime.value),
        entity_id   = entity_id,
        entity_type = entity_type,
        created_by  = author,
        payload     = message.value
      )
    }
  }
}
