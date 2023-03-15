package event.infrastructure.pulsar

import akka.stream.scaladsl._
import event._
import helpers.DisjunctionHelper
import helpers.sorus.Fail
import play.api.Logging
import play.api.libs.json._
import scalaz.{ -\/, \/, \/- }

import scala.util.control.NonFatal

import org.apache.pulsar.client.api.PulsarClientException.AlreadyClosedException
import org.apache.pulsar.client.api.Schema

import com.sksamuel.pulsar4s._
import com.sksamuel.pulsar4s.akka.streams._
import com.sksamuel.pulsar4s.playjson._

private[pulsar] class PulsarListener(pulsar_app: PulsarApplicationClient) extends DisjunctionHelper with Logging {

  /**
   * NB: every message in the stream is acknowledge by the pulsar4s lib
   *
   * @param : close_when_all_message_received : We need to close the consumer when getLastMessageId() == messageId otherwise the stream run endlessly
   * However this throw an AlreadyClosedException we need to handle with Event.empty
   * TODO : find a better way to close the stream
   */
  private[pulsar] def subscribe(
    consumer_config:                 ConsumerConfig,
    criteria:                        EventSearchCriteria,
    close_when_all_message_received: Boolean = false
  ): Source[Fail \/ Event[JsValue], Control] = {
    event_source(consumer_config)
      .map { event =>
        if(
          close_when_all_message_received &&
          event.consumer.getLastMessageId() == event.message.messageId
        ) {
          event.consumer.close()
        }
        event
      }
      .filter { event =>
        criteria.publish_after.forall(_ < event.message.eventTime.value) &&
        criteria.sequence_nb_after.forall(_ < event.message.sequenceId.value)
      }
      .map(event => parse_message(event.message))
      .filter {
        case -\/(_)     => true
        case \/-(event) =>
          criteria.event_name.forall(_ == event.name) &&
          criteria.created_by.forall(_ == event.created_by)
      }
      .recover {
        case err: AlreadyClosedException => {
          logger.info("already closed", err)
          \/-(Event.empty)
        }
        case NonFatal(err)               => {
          val msg = "Unexpected error in pulsar subscription"
          logger.error(msg, err)
          -\/(Fail(msg).withEx(err))
        }
      }
      .filter {
        case -\/(_)     => true
        case \/-(event) => event.name != EventName.EMPTY
      }
  }

  private[this] def event_source(consumer_config: ConsumerConfig): Source[MessageWithConsumer[JsValue], Control] = {
    val consumerFn = () => build_consumer[JsValue](consumer_config)
    Streams.source(consumerFn)
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
      author           <- message.props.get("created_by")  |> "Pulsar parsing error : no created by"
    } yield {
      Event(
        id          = EventId(event_id),
        name        = EventName(event_name_props),
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
