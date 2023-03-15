package event.infrastructure

import akka.Done
import akka.stream.Materializer
import akka.stream.scaladsl._
import event.infrastructure.pulsar.EventBusClient
import event.{ Event, EventBusListener }
import helpers.StreamHelper
import helpers.sorus.Fail
import play.api.Logging
import play.api.libs.json.JsValue
import scalaz.std.scalaFuture._
import scalaz.{ -\/, \/, \/-, EitherT }
import utils.LoggerUtils

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

import com.sksamuel.pulsar4s.akka.streams.Control

/**
  * This is just a wrapper on EventBusClient that run the stream
  *
  * The key part id
  *  - stream_name : define the topic name based on class name
  *  - handleEvent : do the real job
  */
trait StreamEventBusListener extends EventBusListener with Logging {

  protected val stream_name: String = this.getClass().getName()

  def pulsar_client: EventBusClient
  def materializer: Materializer
  implicit def ec: ExecutionContext

  def handleEvent(event: Event[JsValue]): EitherT[Future, Fail, _]

  final def register(subscription_name: String, topic_pattern: String): Future[Done] = {
    logger.info(
      s"[$stream_name] register listener ${subscription_name} on topic ${pulsar_client.topic_name(topic_pattern)}"
    )
    register_source(topic_pattern, pulsar_client.subscribe(subscription_name, topic_pattern))
  }

  private[this] def register_source(
    topic_pattern: String,
    pulsar_source: Source[Fail \/ Event[JsValue], Control]
  ): Future[Done] = {
    val sink   = Sink.ignore
    val source = pulsar_source
      .map {
        case -\/(fail)  => logger.error(s"[$stream_name] fail to parse incoming event ${fail.userMessage()}")
        case \/-(event) => handle(event)
      }

    val stream: RunnableGraph[Future[Done]] = source.toMat(sink)(Keep.right)
    run_stream(topic_pattern, stream)
  }

  private[this] def handle(event: Event[JsValue]): Future[Fail \/ _] = {
    logger.info(s"[Pulsar][$stream_name] handle incoming event ${event.name}")
    handleEvent(event)
      .leftMap(_.withEx(Fail(s"[$stream_name] fail to handle event $event")))
      .leftMap { fail =>
        LoggerUtils.log_error(fail)(logger)
        fail
      }
      .run
  }

  private[this] def run_stream(topic_pattern: String, stream: RunnableGraph[Future[Done]]): Future[Done] = {
    logger.info(s"[$stream_name] stream has started")

    val streamComplete = StreamHelper.with_default_supervision(stream).run()(materializer)

    streamComplete.onComplete {
      case Success(_) => logger.info(s"[$stream_name][$topic_pattern] stream has completed successfully")
      case Failure(e) => logger.error(s"[$stream_name][$topic_pattern] stream has completed with an error", e)
    }(ec)

    streamComplete
  }
}
