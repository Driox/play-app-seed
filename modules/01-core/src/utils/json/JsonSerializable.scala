package utils.json

import play.api.libs.json.JsValue

trait JsonSerializable {
  def toJson(): JsValue
}
