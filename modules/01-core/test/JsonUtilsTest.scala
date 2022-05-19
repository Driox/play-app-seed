import play.api.libs.json._
import utils.JsonUtils

import org.scalatestplus.play.PlaySpec

class JsonUtilsTest extends PlaySpec {

  "JsonUtils" should {
    "hash a json object according to its data" in {
      val json_1 = Json.parse(
        """{
          "name": "toto",
          "age": 12,
          "city": "Paris"
          }"""
      )
      val json_2 = Json.parse(
        """{
          "age": 12,
          "city": "Paris",
          "name": "toto"
          }"""
      )

      val hash_1 = JsonUtils.hash(json_1)
      val hash_2 = JsonUtils.hash(json_2)

      hash_1 mustBe hash_2
    }

    "hash a json primitive" in {
      JsonUtils.hash(JsString("toto")) mustBe "6BBAAEB62A76C9D1F00998ECCDFC172ED7466FE4867CE85C4FB377FAF3ACF76DD4E27F91087E48203A9A8BBC3063E1BCBFD361E503F312575409E9A0694B9B30"
      JsonUtils.hash(JsNumber(12)) mustBe "5AADB45520DCD8726B2822A7A78BB53D794F557199D5D4ABDEDD2C55A4BD6CA73607605C558DE3DB80C8E86C3196484566163ED1327E82E8B6757D1932113CB8"
      JsonUtils.hash(JsBoolean(true)) mustBe "9120CD5FAEF07A08E971FF024A3FCBEA1E3A6B44142A6D82CA28C6C42E4F852595BCF53D81D776F10541045ABDB7C37950629415D0DC66C8D86C64A5606D32DE"
      JsonUtils.hash(JsBoolean(false)) mustBe "719FA67EEF49C4B2A2B83F0C62BDDD88C106AAADB7E21AE057C8802B700E36F81FE3F144812D8B05D66DC663D908B25645E153262CF6D457AA34E684AF9E328D"
      JsonUtils.hash(JsNull) mustBe "04F8FF2682604862E405BF88DE102ED7710AC45C1205957625E4EE3E5F5A2241E453614ACC451345B91BAFC88F38804019C7492444595674E94E8CF4BE53817F"
      JsonUtils.hash(JsArray(List(JsString("e1"), JsString("e2")))) mustBe (
        "ADC43A57F0F4CF957C772451DB1F204C2C6A9BE052126425F4210CE01DAF087ADB7A2445A1F398E8E561C74C09A50C7EE33527013618731BE3BCD7306BE5991B"
      )
    }

    "find in a path" in {
      JsonUtils.findInPath(
        Json.obj("user" -> Json.obj("email" -> "aaa@ggg.com")),
        "user.email"
      ) mustBe JsDefined(JsString("aaa@ggg.com"))

      JsonUtils.findInPath(
        JsString("yolo"),
        "user.email"
      ).toString() mustBe JsUndefined(""""yolo" is not an object""").toString()
    }
  }
}
