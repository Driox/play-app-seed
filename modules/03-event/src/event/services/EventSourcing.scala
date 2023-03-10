package event.services

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

  def reload_event[EntityType](
    entity_type:   String,
    entity_id:     Id[EntityType],
    criteria:      EventSearchCriteria = EventSearchCriteria()
  )(implicit read: Reads[EntityType]): Source[Fail \/ Event[EntityType], Control]
}
