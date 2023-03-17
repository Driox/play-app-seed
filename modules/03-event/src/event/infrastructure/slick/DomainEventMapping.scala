package event.infrastructure.slick

import models.dao.EnhancedPostgresDriver
import play.api.db.slick.HasDatabaseConfig
import play.api.libs.json._
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType

trait DomainEventMapping { self: HasDatabaseConfig[EnhancedPostgresDriver] =>

  import profile.api._

  implicit def set_mapper      = MappedColumnType.base[Set[String], List[String]](x => x.toList, x => x.toSet)
  implicit def json_obj_mapper = MappedColumnType.base[JsObject, JsValue](x => x, x => x.as[JsObject])

}
