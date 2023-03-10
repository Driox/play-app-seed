package event.services

import event.{ Event, EventSearchCriteria }

import scala.concurrent.Future

import scalaz.\/
import helpers.sorus.Fail
import akka.stream.scaladsl.Source
import play.api.libs.json.JsValue
import com.sksamuel.pulsar4s.Subscription
import com.sksamuel.pulsar4s.akka.streams.Control

trait EventBusPublisher {

  def publish[EVENT_BODY](topic_name: String, event: Event[EVENT_BODY]): Future[Fail \/ _]

}
trait EventBusListener {

  def subscribe(
    subscription_name: Subscription,
    topic_name:        String,
    criteria:          EventSearchCriteria = EventSearchCriteria()
  ): Source[Fail \/ Event[JsValue], Control]

}
