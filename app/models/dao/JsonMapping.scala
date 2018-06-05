package models.dao

import play.api.libs.json.{ JsValue, Json }
import slick.jdbc.{ GetResult, JdbcProfile }

class JsonMapping(val driver: JdbcProfile) {

  import driver.api._

  def jsonToStringMapper = MappedColumnType.base[JsValue, String](
    json => Json.prettyPrint(json),
    str => Json.parse(str).as[JsValue]
  )

  object GetJsValue extends scala.AnyRef with slick.jdbc.GetResult[JsValue] {
    def apply(rs: slick.jdbc.PositionedResult): JsValue = {
      val rez = GetResult.GetString(rs)
      Json.parse(rez).as[JsValue]
    }
  }

  object GetJsValueOption extends scala.AnyRef with slick.jdbc.GetResult[Option[JsValue]] {
    def apply(rs: slick.jdbc.PositionedResult): Option[JsValue] = {
      val rez = GetResult.GetStringOption(rs)
      rez.map(Json.parse(_).as[JsValue])
    }
  }
}
