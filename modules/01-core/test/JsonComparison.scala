package test

import diffson._
import diffson.jsonpatch._
import diffson.jsonpatch.lcsdiff.remembering._
import diffson.lcs._
import diffson.playJson.DiffsonProtocol._
import diffson.playJson._
import play.api.libs.json._

import org.scalatest.matchers.{ MatchResult, Matcher }

trait JsonComparison {
  def matchJson(right: JsValue): JsonDiffForMatcher = new JsonDiffForMatcher(right)

  class JsonDiffForMatcher(right: JsValue) extends Matcher[JsValue] {

    override def apply(left: JsValue): MatchResult = {

      implicit val lcs = new Patience[JsValue]

      implicit val format = Json.format[JsonPatch[JsValue]]

      val json_diff: String                = Json.prettyPrint(Json.toJson(diff(left, right)))
      val matches: Boolean                 = (right == left)
      val rawFailureMessage: String        = "{0}\ndid not match\n{1}\n\nJson Diff:\n{2}"
      val rawNegatedFailureMessage: String = "Json should not have matched {0} matched {1}\n\nJson Diff:\n{2}"
      val args: IndexedSeq[Any]            = IndexedSeq(
        left,
        right,
        json_diff
      )
      MatchResult(matches, rawFailureMessage, rawNegatedFailureMessage, args)
    }
  }
}

object JsonComparison extends JsonComparison
