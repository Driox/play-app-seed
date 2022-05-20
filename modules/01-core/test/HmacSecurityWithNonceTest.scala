import play.api.test.{ DefaultAwaitTimeout, FutureAwaits }
import security.hmac._
import security.hmac.verifier._
import test.mock.CacheMock
import utils.{ StringUtils, TimeUtils }

import scala.concurrent.ExecutionContext.Implicits.global

import org.scalatestplus.play.PlaySpec

class HmacSecurityWithNonceTest extends PlaySpec with FutureAwaits with DefaultAwaitTimeout with HmacSecurityHelper {

  "HmacSecurityWithNonce" should {
    "refuse a request without a nonce" in {
      val now            = TimeUtils.now_iso()
      val cache          = new CacheMock()
      val nonce_verifier = new NonceVerifier(
        cache
        //,Configuration.from(Map("hmacsecurity.time_window" -> 10))
      )
      val security       =
        new HmacCoreSecurity(HmacSecurityConfig("a_key", "a_secret"), List(nonce_verifier.verify))

      val req = HmacSecurityRequest(
        "GET",
        None,
        None,
        now
      )

      val auth = security.authorization_header(req)

      auth must startWith("HMAC a_key:")

      verify_security(security, auth, req) mustBe false
    }

    "compute authorization header with empty body and a nonce" in {
      val now            = TimeUtils.now_iso()
      val cache          = new CacheMock()
      val nonce_verifier = new NonceVerifier(
        cache
        //, Configuration.from(Map("hmacsecurity.time_window" -> 10))
      )
      val security       =
        new HmacCoreSecurity(HmacSecurityConfig("a_key", "a_secret"), List(nonce_verifier.verify))

      val req = HmacSecurityRequest(
        "GET",
        None,
        None,
        now,
        StringUtils.randomAlphanumericString(10),
        List("nonce" -> StringUtils.randomAlphanumericString(10))
      )

      val auth = security.authorization_header(req)

      auth must startWith("HMAC a_key:")

      verify_security(security, auth, req) mustBe true

      // nonce used only once
      verify_security(security, auth, req) mustBe false
    }

  }
}
