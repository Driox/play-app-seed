package event.infrastructure.slick

import event._
import models.dao.EnhancedPostgresDriver
import play.api.db.slick.HasDatabaseConfig
import play.api.libs.json._

trait DomainEventMapping { self: HasDatabaseConfig[EnhancedPostgresDriver] =>

  import profile.api._

  // implicit def event_id_mapper   = MappedColumnType.base[EventId, String](_.toString(), str => EventId(str))
  // implicit def event_name_mapper = MappedColumnType.base[EventName, String](_.toString(), str => EventName(str))
  // implicit def timestamp_mapper  = MappedColumnType.base[Timestamp, Long](x => x, x => Timestamp(x))

  implicit def set_mapper      = MappedColumnType.base[Set[String], List[String]](x => x.toList, x => x.toSet)
  implicit def json_obj_mapper = MappedColumnType.base[JsObject, JsValue](x => x, x => x.as[JsObject])

}
