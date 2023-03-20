package utils.json

import play.api.libs.json.JsValue

/**
  * This is a useful trait to build union type with JsValue
  * It avoid passing a implicit Reads / Writes to every layer of code
  *
  * In scala 3 we would have used true union type like {{ A | JsValue }}
  * We can't do this easilly in scala 2 so we have to pattern match on JsValue and this type
  */
trait JsonSerializable {
  def toJson(): JsValue
}
