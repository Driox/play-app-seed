package models.dao

import com.github.tototoshi.slick.GenericJodaSupport
import play.api.libs.json.{JsValue, Json}

import scala.util.Try
import slick.jdbc._
import slick.jdbc.GetResult

object EnhancedH2Driver
    extends GenericJodaSupport(H2Profile)
    with JdbcProfile
    with JsonSupportH2
    with CollectionSupport {

}

trait JsonSupportH2 extends JsonSupport { self: com.github.tototoshi.slick.GenericJodaSupport =>

  import self.driver.api._

  implicit def jsonToStringMapper = MappedColumnType.base[JsValue, String](
    json => Json.prettyPrint(json),
    str => Json.parse(str).as[JsValue]
  )

  implicit def jsonOptToStringMapper = MappedColumnType.base[Option[JsValue], String](
    json => json.map(Json.prettyPrint(_)).getOrElse(""),
    str => Try { Json.parse(str) }.toOption
  )

  implicit object GetJsValue extends scala.AnyRef with slick.jdbc.GetResult[JsValue] {
    def apply(rs: slick.jdbc.PositionedResult): JsValue = {
      val rez = GetResult.GetString(rs)
      Json.parse(rez).as[JsValue]
    }
  }

  implicit object GetJsValueOption extends scala.AnyRef with slick.jdbc.GetResult[Option[JsValue]] {
    def apply(rs: slick.jdbc.PositionedResult): Option[JsValue] = {
      val rez = GetResult.GetStringOption(rs)
      rez.map(Json.parse(_).as[JsValue])
    }
  }
}
