package event

import akka.stream.scaladsl.Source
import event.{ Event, EventSearchCriteria }
import helpers.sorus.Fail
import play.api.libs.json.JsValue
import scalaz.\/

import scala.concurrent.Future

import com.sksamuel.pulsar4s.MessageId
import com.sksamuel.pulsar4s.akka.streams.Control

trait EventBusPublisher {

  def publish[EVENT_BODY](topic_name: String, event: Event[EVENT_BODY]): Future[Fail \/ MessageId]

}
trait EventBusListener {

  def subscribe(
    subscription_name: String,
    topic_name:        String,
    criteria:          EventSearchCriteria = EventSearchCriteria()
  ): Source[Fail \/ Event[JsValue], Control]

}
