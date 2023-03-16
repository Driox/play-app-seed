package event.infrastructure.pulsar

import event.Event
import helpers.sorus.Fail
import helpers.sorus.SorusDSL.Sorus
import play.api.Logging
import play.api.libs.json.JsValue
import scalaz.Scalaz.ToEitherOps
import scalaz.\/

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

import org.apache.pulsar.client.api.Schema

import com.sksamuel.pulsar4s._
import com.sksamuel.pulsar4s.playjson._

private[pulsar] class PulsarPublisher(pulsar_app: PulsarApplicationClient) extends Logging with Sorus {

  private[this] implicit val ec: ExecutionContext = pulsar_app.ec

  def publish[EVENT_BODY](event: Event[EVENT_BODY], producer_config: ProducerConfig): Future[Fail \/ MessageId] = {
    logger.info(s"[Pulsar]publish event ${event.name} => ${producer_config.topic.name}")

    def publish(): Future[Fail \/ MessageId] = {
      val producer                          = build_producer[JsValue](producer_config)
      val message: ProducerMessage[JsValue] = build_message(event)

      val result = producer.sendAsync(message)
      result.onComplete { maybe_msg_id =>
        producer.closeAsync
        onCompleteLogError(event, producer_config)(maybe_msg_id.failed.toOption)
      }
      result ?| "error.pulsar_publisher.publish.unexpected_error"
    }
    Try(publish()) match {
      case Success(result) => result
      case Failure(err)    => {
        onCompleteLogError(event, producer_config)(Some(err))
        Future.successful(Fail("error.publish.event.on.pulsar").withEx(err).left[MessageId])
      }
    }
  }

  /**
   * compression summary :
   *   SNAPPY : faster read / write
   *   ZLIB   : smaller size on disk
   */
  // TODO : check config for batching
  private[this] def build_producer[T](producer_config: ProducerConfig)(implicit schema: Schema[T]): Producer[T] = {
    pulsar_app.pulsar_client.producer[T](producer_config)
  }

  private[this] def onCompleteLogError(
    event:           Event[_],
    producer_config: ProducerConfig
  )(err:             Option[Throwable]): Unit = {
    err match {
      case Some(err) => logger.error(
          s"Error publishing event ${event.name} on topic ${producer_config.topic.name} event full $event",
          err
        )
      case None      => ()
    }
  }

  /**
   * all the field of the Event is store in a part or another of the pulsar's Message
   */
  private[this] def build_message[EVENT_BODY](event: Event[EVENT_BODY]): ProducerMessage[JsValue] = {
    DefaultProducerMessage(
      key       = Some(event.entity_id),
      // TODO event : test it with decuplication enabled on topic https://pulsar.apache.org/docs/2.11.x/cookbooks-deduplication/
      // sequenceId = Some(SequenceId(event.sequence_nb)),
      props     = pulsar_app.default_properties() ++ event.metadata(),
      value     = event.payloadAsJson(),
      eventTime = Some(EventTime(event.created_at))
    )
  }

}
