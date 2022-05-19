package utils.json

import play.api.Logging
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.util.Try

/**
 * help : http://kailuowang.blogspot.fr/2013/11/addremove-fields-to-plays-default-case.html
 *
 * usage :
 * val customWrites: Writes[Person] = Json.Writes[Person].
 *                                 addField("isAdult", _.isAdult). // isAdult is a method on Person
 *                                 removeField("age")
 */
class OWritesOps[A](writes: Writes[A]) extends Logging {
  def addField[T: Writes](fieldName: String, field: A => T): Writes[A] = {
    Try {
      val owrites: OWrites[A] = writes.asInstanceOf[OWrites[A]]
      (owrites and (__ \ fieldName).write[T])((a: A) => (a, field(a)))
    }.getOrElse {
      logger.error(
        s"[JSON] You try to add field $fieldName on a json write that is not an object. You can only add field to json object not on array / null / boolean / etc..."
      )
      writes
    }
  }

  def removeField(fieldName: String): Writes[A] = OWrites { a: A =>
    val transformer = (__ \ fieldName).json.prune
    Json.toJson(a)(writes).validate(transformer).get
  }
}

object JsonOWritesOps {
  implicit def from[A](writes: Writes[A]): OWritesOps[A] = new OWritesOps(writes)
}
