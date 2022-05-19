package utils.json

import play.api.libs.json._

import scala.collection.Seq

object JsonErrorFormat {
  def format(errors: Seq[(JsPath, Seq[JsonValidationError])]): String = {
    val translated_error = errors.map(a_path => s"${a_path._1} -> ${a_path._2.map(err => err.message)}")
    translated_error.mkString("\n")
  }
}
