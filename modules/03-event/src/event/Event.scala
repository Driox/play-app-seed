package event

import event.{ EventId, Timestamp }
import play.api.libs.json._
import utils.json.JsonSerializable

/**
 * @id         : event unique id, evt_<uuid>
 * @name       : name of the event
 * @sequence_nb: ordering of event for a given entity_id
 * @created_at : timestamp in unix sens
 * @created_by : the id of the user / entity that generate this event
 * @entity_id  : id of the aggregate enity
 * @paylod     : content of the event, mostly as JsObject
 */
case class Event[+A](
  id:          EventId     = EventId.generate(),
  name:        EventName,
  sequence_nb: Long,
  created_at:  Timestamp   = Timestamp.generate(),
  created_by:  String,
  entity_id:   String,
  entity_type: String,
  payload:     A,
  tags:        Set[String] = Set()
) {
  def payloadAsJson(): JsValue        = {
    payload match {
      case json: JsValue       => json
      case x: JsonSerializable => x.toJson()
      case _                   => JsString(s"Json serialisation for ${payload.getClass()} not implemented yet")
    }
  }
  def metadata(): Map[String, String] = Map(
    "id"          -> id,
    "event_name"  -> name.toString,
    "created_by"  -> created_by,
    "entity_id"   -> entity_id, // entity_id is set on the key in pulsar implem
    "entity_type" -> entity_type
  )
}

case class EventSearchCriteria(
  event_name:        Option[EventName] = None,
  created_by:        Option[String]    = None,
  publish_after:     Option[Timestamp] = None,
  sequence_nb_after: Option[Long]      = None
)
