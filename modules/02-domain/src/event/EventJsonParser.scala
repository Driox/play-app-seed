package event

import play.api.libs.json.{ JsValue, Writes }
trait EventJsonParser {

  implicit val event_format: Writes[Event[JsValue]] = Event.event_writer
}
