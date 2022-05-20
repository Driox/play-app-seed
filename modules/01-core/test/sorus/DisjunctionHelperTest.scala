package helpers

import helpers.sorus.Fail
import play.api.libs.json._
import scalaz._

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DisjunctionHelperTest extends AnyFlatSpec with Matchers with DisjunctionHelper {

  "DisjunctionHelper " should "transform option" in {
    (Some("coucou") |> "some error") shouldBe \/-("coucou")
    (None           |> "some error") shouldBe -\/(Fail("some error"))
  }

  "DisjunctionHelper " should "transform boolean" in {
    (true  |> "some error") shouldBe \/-(true)
    (false |> "some error") shouldBe -\/(Fail("some error"))
  }

  "DisjunctionHelper " should "transform either" in {
    val right_input: Either[Fail, String] = Right("coucou")
    (right_input |> "some error") shouldBe \/-("coucou")

    val left_input: Either[Fail, String] = Left(Fail("error lvl 1"))
    (left_input |> "some error") shouldBe -\/(Fail("error lvl 1").withEx("some error"))
  }

  "DisjunctionHelper " should "transform disjunction" in {
    val right_input: Fail \/ String = \/-("coucou")
    (right_input |> "some error") shouldBe \/-("coucou")

    val left_input: Fail \/ String = -\/(Fail("error lvl 1"))
    (left_input |> "some error") shouldBe -\/(Fail("error lvl 1").withEx("some error"))
  }

  "DisjunctionHelper " should "transform jsresult" in {
    val right_input: JsResult[String] = JsSuccess("coucou")
    (right_input |> "some error") shouldBe \/-("coucou")

    val path_node: PathNode          = KeyPathNode("user")
    val js_path: JsPath              = JsPath(List(path_node))
    val left_input: JsResult[String] = JsError(List((js_path, List(JsonValidationError("Not found in path")))))
    (left_input |> "some error") shouldBe -\/(Fail(
      "some error",
      Some(\/-(Fail(
        "obj.user -> JsonValidationError(List(Not found in path),List())",
        Some(\/-(Fail("Error parsing json")))
      )))
    ))
  }

}
