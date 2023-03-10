import utils.StringUtils

import org.scalatestplus.play.PlaySpec

class UtilsTest extends PlaySpec {

  "StringUtils" should {

    "validate an email" in {
      val email_1 = "toto@gmail.com"
      val email_2 = "toto.gmail@com"

      StringUtils.isEmail(email_1) mustBe true
      StringUtils.isEmail(email_2) mustBe false
      StringUtils.isEmail(null) mustBe false
      StringUtils.isEmail("") mustBe false
    }

    "validate an uuid" in {
      val uuid_1 = "a6075fc2-07b7-4a71-9f99-e648daeda35e"
      val uuid_2 = "toto.gmail@com"
      val uuid_3 = StringUtils.generateUuid()

      StringUtils.isUuid(uuid_1) mustBe true
      StringUtils.isUuid(uuid_2) mustBe false
      StringUtils.isUuid(uuid_3) mustBe true
      StringUtils.isUuid(null) mustBe false
      StringUtils.isUuid("") mustBe false
    }

    "validate an ip" in {
      val ip_1 = "192.128.1.1"
      val ip_2 = "567.98.3.12"
      val ip_3 = "ldfkjlgkjlgdjfgl"

      StringUtils.isIp(ip_1) mustBe true
      StringUtils.isIp(ip_2) mustBe false
      StringUtils.isIp(ip_3) mustBe false
      StringUtils.isIp(null) mustBe false
      StringUtils.isIp("") mustBe false
    }
  }
}
