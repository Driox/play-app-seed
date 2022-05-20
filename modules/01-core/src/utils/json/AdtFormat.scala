package utils.json

import play.api.libs.json._

object AdtFormat {
  def format[S, ADT <: Product](listing: Set[ADT]): Format[ADT] = {
    new Format[ADT] {
      def writes(input: ADT) = JsString(input.productPrefix)
      def reads(json: JsValue) = {
        listing.filter(v => JsString(v.productPrefix) == json).headOption match {
          case Some(value) => JsSuccess(value)
          case None        => JsError(s"Can't parse ${json}")
        }
      }
    }
  }
}
