package event.infrastructure.pulsar

import akka.Done
import akka.stream.scaladsl.Source
import akka.stream.stage.{ AsyncCallback, GraphStageLogic, GraphStageWithMaterializedValue, OutHandler }
import akka.stream.{ Attributes, Outlet, SourceShape }

import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.util.control.NonFatal
import scala.util.{ Failure, Success, Try }

import org.apache.pulsar.client.api.ConsumerStats
import org.apache.pulsar.client.api.PulsarClientException.AlreadyClosedException

import com.sksamuel.exts.Logging
import com.sksamuel.pulsar4s.akka.streams.Control
import com.sksamuel.pulsar4s.{ Consumer, ConsumerMessage, MessageId }

/**
  * We overload the pulsar4s lib to be able to access the consumer in the akka stream
  * Mainly because we need to check consumer.getLastMessageId
  */
object Streams {

  /**
   * Create an Akka Streams source for the given [[Consumer]] that produces [[ConsumerMessage]]s and auto-acknowledges.
   *
   * @param create a function to create a new [[Consumer]].
   * @param seek an optional [[MessageId]] to seek to. Note that seeking will not work on multi-topic subscriptions.
   *             Prefer setting `subscriptionInitialPosition` in `ConsumerConfig` instead if you need to start at the
   *             earliest or latest offset.
   * @return the new [[Source]].
   */
  def source[T](create: () => Consumer[T], seek: Option[MessageId] = None): Source[MessageWithConsumer[T], Control] =
    Source.fromGraph(new PulsarSourceGraphStageWithConsumer(create, seek))

}

case class MessageWithConsumer[T](
  consumer: Consumer[T],
  message:  ConsumerMessage[T]
)

private class PulsarSourceGraphStageWithConsumer[T](create: () => Consumer[T], seek: Option[MessageId])
  extends GraphStageWithMaterializedValue[SourceShape[MessageWithConsumer[T]], Control]
    with Logging {

  private val out                                         = Outlet[MessageWithConsumer[T]]("pulsar.out")
  override def shape: SourceShape[MessageWithConsumer[T]] = SourceShape(out)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Control) = {

    val logic: GraphStageLogic with Control = new GraphStageLogic(shape) with OutHandler with Control {
      setHandler(out, this)

      implicit def ec: ExecutionContext = materializer.executionContext

      @inline private def consumer: Consumer[T]                           =
        consumerOpt.getOrElse(throw new IllegalStateException("Consumer not initialized!"))
      private var consumerOpt: Option[Consumer[T]]                        = None
      private val receiveCallback: AsyncCallback[Try[ConsumerMessage[T]]] = getAsyncCallback {
        case Success(msg) => {
          logger.debug(s"Msg received $msg")
          push(out, MessageWithConsumer(consumer, msg))
          consumer.acknowledge(msg.messageId)
        }
        case Failure(e)   => {
          e match {
            case err: AlreadyClosedException => failStage(err) // fail silently, check comment on PulsarListener
            case _                           => {
              logger.warn("Error when receiving message", e)
              failStage(e)
            }
          }
        }
      }
      private val stopped: Promise[Done]                                  = Promise()
      private val stopCallback: AsyncCallback[Unit]                       = getAsyncCallback { _ => completeStage() }

      override def preStart(): Unit = {
        try {
          val consumer = create()
          consumerOpt = Some(consumer)
          stopped.future.onComplete { _ =>
            // Note: unlike the committable source, we don't expect acks so we can close immediately
            close()
          }
          seek foreach consumer.seek
        } catch {
          case NonFatal(e) =>
            logger.error("Error creating consumer!", e)
            failStage(e)
        }
      }

      override def onPull(): Unit = {
        logger.debug("Pull received; asking consumer for message")
        consumer.receiveAsync.onComplete(receiveCallback.invoke(_))
      }

      override def postStop(): Unit = stopped.success(Done)

      override def complete()(implicit ec: ExecutionContext): Future[Done] = {
        stopCallback.invoke(())
        stopped.future
      }

      private def close()(implicit ec: ExecutionContext): Future[Done] =
        consumerOpt.fold(Future.successful(Done))(_.closeAsync.map(_ => Done))

      override def shutdown()(implicit ec: ExecutionContext): Future[Done] =
        for {
          _ <- complete()
          _ <- close()
        } yield Done

      override def stats: ConsumerStats = consumer.stats
    }

    (logic, logic)
  }
}
