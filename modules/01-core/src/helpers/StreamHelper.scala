package helpers

import akka.stream._
import akka.stream.scaladsl._
import helpers.sorus.Fail
import play.api.Logging
import play.api.libs.json._
import scalaz.{ -\/, \/, \/- }

import scala.collection.immutable.SortedMap
import scala.concurrent.Future
import scala.util.control.NonFatal

case class StreamResult(
  element_processed: Long              = 0L,
  element_succeeded: Long              = 0L,
  errors:            Map[String, Fail] = SortedMap()
) {
  def is_success(): Boolean = (element_processed == element_succeeded) && errors.isEmpty && (element_succeeded >= 0)
  def toJson(): JsValue     = {
    if(is_success()) {
      Json.obj(
        "tech_message" -> JsString("import.success"),
        "user_message" -> JsString("Import executed in success"),
        "status"       -> JsString("success"),
        "processed"    -> JsNumber(element_processed),
        "succeeded"    -> JsNumber(element_succeeded)
      )
    } else {
      Json.obj(
        "tech_message" -> JsString("import.fail"),
        "user_message" -> JsString("Import fail for some lines"),
        "status"       -> JsString("failure"),
        "processed"    -> JsNumber(element_processed),
        "succeeded"    -> JsNumber(element_succeeded),
        "errors"       -> Json.toJson(errors.view.mapValues(_.userMessage()))
      )
    }
  }
}

object StreamHelper extends Logging {

  /**
   * This sink accumulate error with their line number
   *
   * Usually you will use zipWithIndex to get the (_, Long) type
   */
  def folding_sink[A](): Sink[(Fail \/ A, String), Future[StreamResult]] = {
    Sink.fold[StreamResult, (Fail \/ A, String)](StreamResult()) { (acc, elem) =>
      {
        elem match {
          case (\/-(_), _)          => acc.copy(
              element_processed = acc.element_processed + 1,
              element_succeeded = acc.element_succeeded + 1
            )
          case (-\/(fail), line_id) => acc.copy(
              element_processed = acc.element_processed + 1,
              errors            = acc.errors + (line_id -> fail)
            )
        }
      }
    }
  }

  def with_default_supervision[A](stream: RunnableGraph[A]): RunnableGraph[A] = {
    val decider: Supervision.Decider = {
      case NonFatal(e) => {
        logger.error(
          "Non fatal exception in stream, an element has been discarded. This is not a normal behaviour : must be investigate",
          e
        )
        Supervision.Resume
      }
      case _           => Supervision.Stop
    }

    stream.withAttributes(ActorAttributes.supervisionStrategy(decider))
  }
}
