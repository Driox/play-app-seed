package event

trait EventJsonParser {

  implicit val event_format = Event.event_writer
}
