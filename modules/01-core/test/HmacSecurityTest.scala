import play.api.Logging
import play.api.libs.json.Json
import play.api.test.{ DefaultAwaitTimeout, FutureAwaits }
import security.hmac._
import utils.HashUtils

import scala.concurrent.ExecutionContext.Implicits.global

import org.scalatestplus.play.PlaySpec

trait HmacSecurityHelper extends Logging { self: PlaySpec with FutureAwaits with DefaultAwaitTimeout =>
  def verify_security(security: HmacCoreSecurity, auth_header: String, req: HmacSecurityRequest) = {
    val is_secure = await(
      security.verify(auth_header, req).run
    )

    is_secure.fold(
      fail => {
        logger.error(fail.userMessage())
        false
      },
      success => success
    )
  }
}

class HmacSecurityTest extends PlaySpec with FutureAwaits with DefaultAwaitTimeout with HmacSecurityHelper {

  "HmacSecurity" should {

    "not allow empty key or secret" in {

      val err = intercept[IllegalArgumentException] {
        HmacSecurity("", "abc")
      }
      assertResult("requirement failed: key can't be empty")(err.getMessage)

      val err2 = intercept[IllegalArgumentException] {
        HmacSecurity("abc", "")
      }
      assertResult("requirement failed: secret can't be empty")(err2.getMessage)
    }

    "compute authorization header with empty body for GET req" in {
      val now      = "2019-04-09T13:29:22Z"
      val security = HmacSecurity("a_key", "a_secret", time_windows = 3600000)
      val req      = HmacSecurityRequest("GET", None, None, now)
      val auth     = security.authorization_header(req)

      auth must startWith("HMAC a_key:")

      val expected_auth = "HMAC a_key:MjU4YTkyYTVkNjBmZGU1NTJiYzJlZGJkZjU1MTE1MmVlODIxNDA4MQ=="
      verify_security(security, expected_auth, req) mustBe true
    }

    "compute authorization header with empty body for POST req" in {
      val now      = "2019-04-09T13:29:22Z"
      val security = HmacSecurity("a_key", "a_secret", time_windows = 3600000)
      val req      = HmacSecurityRequest("POST", None, None, now)
      val auth     = security.authorization_header(req)

      auth must startWith("HMAC a_key:")

      val expected_auth = "HMAC a_key:OGI1YzEyYzgzMmRjNDgyOWZhODA2NWFkZDcxOTMxZjkyMDA0NjI0ZQ=="
      verify_security(security, expected_auth, req) mustBe true
    }

    "compute authorization header with json body" in {
      val now      = "2019-04-09T13:29:23Z"
      val body     = Json.parse("""{ "user" : { "first_name" : "Jean", "last_name" : "Dupont", "age": 12 }  } """)
      val security = HmacSecurity("a_key", "a_secret", time_windows = 3600000)
      val req      = HmacSecurityRequest("POST", Some(HashUtils.sha512Hash(body.toString())), Some("application-json"), now)

      val auth = security.authorization_header(req)

      auth must startWith("HMAC a_key:")

      val expected_auth = "HMAC a_key:YzA5MTc5OGU5MTk3YmZlMTA2NWY0YjA1OTM4ODA3YTA5ZTllNDczMg=="
      verify_security(security, expected_auth, req) mustBe true
    }

    "fail authorization if outside of time window" in {
      val now      = "2019-04-09T13:29:23Z"
      val body     = Json.parse("""{ "user" : { "first_name" : "Jean", "last_name" : "Dupont", "age": 12 }  } """)
      val security = HmacSecurity("a_key", "a_secret", time_windows = 1)
      val req      = HmacSecurityRequest("POST", Some(HashUtils.sha512Hash(body.toString())), Some("application-json"), now)

      val auth = security.authorization_header(req)

      auth must startWith("HMAC a_key:")

      val expected_auth =
        "HMAC a_key:NjMzMDM5MzEzNzM5Mzg2NTM5MzEzOTM3NjI2NjY1MzEzMDM2MzU2NjM0NjIzMDM1MzkzMzM4MzgzMDM3NjEzMDM5NjUzOTY1MzQzNzMzMzI="
      verify_security(security, expected_auth, req) mustBe false
    }

    "fail authorization if header is wrong" in {
      val now      = "2019-04-09T13:29:23Z"
      val body     = Json.parse("""{ "user" : { "first_name" : "Jean", "last_name" : "Dupont", "age": 12 }  } """)
      val security = HmacSecurity("a_key", "a_secret", time_windows = 3600000)
      val req      = HmacSecurityRequest("POST", Some(HashUtils.sha512Hash(body.toString())), Some("application-json"), now)

      val auth = security.authorization_header(req)

      auth must startWith("HMAC a_key:")

      val fail_auth_header =
        "HMAC a_key:AzUzMzYxMzE2MzM1MzczMzYyMzUzOTY2NjE2NTM3MzczMjY2MzI2MjMzMzgzMDMxMzYzMjYyNjY2NTY1Mzk2NjM2NjIzMDY2MzIzMjM0MzU="
      verify_security(security, fail_auth_header, req) mustBe false
    }
  }
}
