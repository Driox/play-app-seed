package event

import event.{ EventId, Timestamp }

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
  payload:     A,
  tags:        Set[String] = Set()
) {
  def metadata(): Map[String, String] = Map(
    "id"         -> id,
    "created_by" -> created_by,
    "event_name" -> name.toString
  )
}
