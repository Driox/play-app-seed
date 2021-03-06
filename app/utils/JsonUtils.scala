package utils

import play.api.libs.json._
import org.apache.commons.validator.routines.UrlValidator
import helpers.Config

object JsonUtils {

  private implicit val lang = Config.default_lang

  def validUrls: Reads[Seq[String]] = Reads.seq[String].filter(JsonValidationError(m("error.url.malformatted")))(urls => {
    val urlValidator = if (Config.isDev()) new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS) else new UrlValidator();
    urls.filter(!urlValidator.isValid(_)).isEmpty
  })

  def validEmail: Reads[String] = Reads.StringReads.filter(JsonValidationError(m("error.email.malformatted")))(StringUtils.isEmail(_))

  def validePrecision(precision: Int): Reads[Double] = Reads.DoubleReads.filter(JsonValidationError(
    m("error.wallettransferoption.amount.precision")
  ))(NumberUtils.validePrecision(_, precision))

  def validateRegex(regex: String, errorMsg: String): Reads[String] = Reads.StringReads.filter(JsonValidationError(m(errorMsg)))(value => value.matches(regex))

  def validUuid: Reads[String] = Reads.StringReads.filter(JsonValidationError(m("error.uuid.malformatted")))(StringUtils.isUuid(_))

  def validIp: Reads[String] = Reads.StringReads.filter(JsonValidationError(m("error.ip.malformatted")))(StringUtils.isIp(_))

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
      if (removeKeys.contains(current._1)) {
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
}
