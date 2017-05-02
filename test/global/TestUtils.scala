package global

import play.api.libs.json.Json
import utils.StringUtils
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import utils.JsonUtils
import scala.util.matching.Regex
import java.io.File
import java.nio.file.Files

trait TestUtils { self: ApiSpecServer =>

  def checkResultInJsonIsGood(result: String, resultMustBe: String, orMustBe: String) {
    val actual = JsonUtils.replaceElementInJson(Json.parse(result), Map("code" -> Json.toJson("#12345678")))
    val expected = Json.parse(resultMustBe)
    val orExpected = Json.parse(orMustBe)

    actual == expected || actual == orExpected mustBe true
  }

  def checkResultInJsonIsGood(result: String, resultMustBe: String) {
    checkResultInJsonMatch(result, resultMustBe, Map("code" -> Json.toJson("#12345678")))
  }

  def checkResultInJsonMatch(result: String, resultMustBe: String, replacement: Map[String, JsValue]) {
    val actual = JsonUtils.replaceElementInJson(Json.parse(result), replacement)
    val expected = Json.parse(resultMustBe)

    actual mustBe expected
  }

  def removeInResponse(response: String, removeKeys: Seq[String]): String = {
    val json = Json.parse(response).as[JsValue]
    JsonUtils.removeElementInJson(json, removeKeys).toString
  }

  def fileCopy(from: String, to: String) = {
    val f = new File(to)
    if (f.exists) {
      f.delete
    }

    Files.copy(new File(from).toPath(), f.toPath())
  }
}
