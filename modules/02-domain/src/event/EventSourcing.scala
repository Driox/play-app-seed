package event

import akka.stream.scaladsl.Source
import event.{ Event, EventSearchCriteria }
import helpers.sorus.Fail
import play.api.libs.json.Reads
import scalaz.\/
import tagged.Tags.Id

import scala.concurrent.Future

import com.sksamuel.pulsar4s.MessageId
import com.sksamuel.pulsar4s.akka.streams.Control

trait EventSourcing {

  def persist[EVENT_BODY](event: Event[EVENT_BODY]): Future[Fail \/ MessageId]

  def reload_event[EntityIdType, EventPayload](
    entity_type:   String,
    entity_id:     Id[EntityIdType],
    criteria:      EventSearchCriteria = EventSearchCriteria()
  )(implicit read: Reads[EventPayload]): Source[Fail \/ Event[EventPayload], Control]
}
