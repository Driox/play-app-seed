package utils

import play.api.i18n.Lang
import play.api.libs.json._

import scala.annotation.tailrec
import org.apache.commons.validator.routines.UrlValidator

object JsonUtils {

  private implicit val lang = Lang("en-US")

  private[this] def sort_fields(js: JsValue): JsValue = js match {
    case JsObject(fields) => JsObject(fields.toSeq.sortBy(_._1).map { case (key, value) => (key, sort_fields(value)) })
    case JsArray(array)   => JsArray(array.map(e => sort_fields(e)))
    case other            => other
  }

  def hash(input: JsValue): String = {
    val sorted_data = sort_fields(input).toString()
    HashUtils.sha512Hash(sorted_data)
  }

  def formatJsonError(errors: scala.collection.Seq[(JsPath, scala.collection.Seq[JsonValidationError])]): String = {
    errors.map(err => err._1.path.mkString("/") + " -> " + err._2.map(_.message).mkString(", ")).mkString("\n")
  }

  def validUrls(is_dev: Boolean): Reads[Seq[String]] =
    Reads.seq[String].filter(JsonValidationError(m("error.url.malformatted")))(urls => {
      val urlValidator = if(is_dev) new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS) else new UrlValidator()
      urls.filter(!urlValidator.isValid(_)).isEmpty
    })

  def validEmail: Reads[String] =
    Reads.StringReads.filter(JsonValidationError(m("error.email.malformatted")))(StringUtils.isEmail(_))

  def validateRegex(regex: String, errorMsg: String): Reads[String] =
    Reads.StringReads.filter(JsonValidationError(m(errorMsg)))(value => value.matches(regex))

  def validUuid: Reads[String] =
    Reads.StringReads.filter(JsonValidationError(m("error.uuid.malformatted")))(StringUtils.isUuid(_))

  def validIp: Reads[String] =
    Reads.StringReads.filter(JsonValidationError(m("error.ip.malformatted")))(StringUtils.isIp(_))

  def removeElementInJson(json: JsValue, removeKeys: Seq[String]): JsValue = {
    val jsArrayOpt = json.asOpt[JsArray].map(v => {
      removeElementInJson(v, removeKeys)
    })

    val jsObject = json.asOpt[JsObject].map(v => {
      removeElementInJson(v, removeKeys)
    })

    jsArrayOpt.orElse(jsObject).getOrElse(json)
  }

  private[this] def removeElementInJson(json: JsArray, removeKey: Seq[String]): JsArray = {
    JsArray(json.value.map(v => removeElementInJson(v, removeKey)))
  }

  private[this] def removeElementInJson(json: JsObject, removeKeys: Seq[String]): JsObject = {
    val fields = json.fields.foldLeft(Seq[(String, JsValue)]())((acc, current) => {
      if(removeKeys.contains(current._1)) {
        acc
      } else {
        val result = (current._1, removeElementInJson(current._2, removeKeys))
        acc :+ result
      }
    })
    JsObject(fields)
  }

  def replaceElementInJson(json: JsValue, new_value_map: Map[String, JsValue]): JsValue = {
    val jsArrayOpt = json.asOpt[JsArray].map(v => {
      replaceElementInJson(v, new_value_map)
    })

    val jsObject = json.asOpt[JsObject].map(v => {
      replaceElementInJson(v, new_value_map)
    })

    jsArrayOpt.orElse(jsObject).getOrElse(json)
  }

  private[this] def replaceElementInJson(json: JsArray, new_value_map: Map[String, JsValue]): JsArray = {
    JsArray(json.value.map(v => replaceElementInJson(v, new_value_map)))
  }

  private[this] def replaceElementInJson(json: JsObject, new_value_map: Map[String, JsValue]): JsObject = {
    val fields = json.fields.map {
      case (name, value) => {
        val new_value = new_value_map.get(name).getOrElse(replaceElementInJson(value, new_value_map))
        (name, new_value)
      }
    }

    JsObject(fields)
  }

  def findInPath(json: JsValue, path: String): JsLookupResult = {

    val pattern_array = "\\[([0-9]+)\\]".r
    val pattern       = "(.*)\\[([0-9]+)\\]".r
    @tailrec
    def findInPathRec(result: JsLookupResult, path: List[String]): JsLookupResult = {
      path match {
        case Nil                          => result
        case pattern_array(index) :: tail => findInPathRec((result \ index.toInt), tail)
        case pattern(name, index) :: tail => findInPathRec((result \ name \ index.toInt), tail)
        case head :: tail                 => findInPathRec((result \ head), tail)
      }
    }

    val paths = path.split("\\.").toList
    findInPathRec(JsDefined(json), paths)
  }
}
