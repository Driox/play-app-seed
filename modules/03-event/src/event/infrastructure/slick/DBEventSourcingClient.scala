package event.infrastructure.slick

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import event._
import helpers.DisjunctionHelper
import helpers.sorus.Fail
import helpers.sorus.SorusDSL.Sorus
import play.api.Configuration
import play.api.libs.json._
import scalaz.{ -\/, \/, \/- }
import tagged.Tags.Id

import javax.inject.{ Inject, Singleton }
import scala.concurrent.Future

@Singleton
class DBEventSourcingClient @Inject() (
  config:     Configuration,
  system:     ActorSystem,
  repository: EventRepository
) extends EventSourcing with Sorus with DisjunctionHelper {

  def persist[EVENT_BODY](event: Event[EVENT_BODY]): Future[Fail \/ Int] = {
    for {
      json_event <- build_event(event)             ?| ()
      result     <- repository.persist(json_event) ?| ()
    } yield {
      result
    }
  }

  private[this] def build_event[EVENT_BODY](event: Event[EVENT_BODY]): Fail \/ Event[JsObject] = {
    for {
      payload <- event.payloadAsJson().validate[JsObject] |> s"error parsing event payload ${event.payloadAsJson()} is not a JsObject"
    } yield {
      event.copy[JsObject](payload = payload)
    }
  }

  def reload_event[EntityIdType, EventPayload](
    entity_type:   String,
    entity_id:     Id[EntityIdType],
    criteria:      EventSearchCriteria = EventSearchCriteria()
  )(implicit read: Reads[EventPayload]): Source[Fail \/ Event[EventPayload], _] = {

    val source = Source.fromPublisher(repository.search(entity_id, entity_type, criteria))
    source.map(entity => parseJsonToEvent(entity))
  }

  private[this] def parseJsonToEvent[EventPayload](event_json: Event[JsValue])(implicit
    read:                                                      Reads[EventPayload]
  ): Fail \/ Event[EventPayload] = {
    event_json.payload.validate[EventPayload] match {
      case JsSuccess(entity, _) => \/-(event_json.copy[EventPayload](payload = entity))
      case JsError(err)         => -\/(Fail(err))
    }
  }

}
