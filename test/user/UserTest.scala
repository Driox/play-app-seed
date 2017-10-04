package test.user

import global.{ApiSpecServer, TestUtils}
import models.Users
import notifier.WelcomeNotifier

import scala.concurrent.Future
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.scalatest.mockito.MockitoSugar
import test.global.CtrlHelper
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * In this test we test the full application stack : ctrl + models + db (h2) + evolutions
 */
class UserTest extends ApiSpecServer with TestUtils with MockitoSugar with CtrlHelper {

  "The application " should {

    "return a 303 with email in session for a logged user" in {
      val welcomeNotifier = mock[WelcomeNotifier]
      val userDao: Users = app.injector.instanceOf(classOf[Users])

      val email = "jean.dupont@gmail.com"
      val req = FakeRequest().withFormUrlEncodedBody("email" -> email, "password" -> "12345678")
      val result: Future[Result] = stubify(new controllers.AuthenticationController(welcomeNotifier, userDao))
        .authenticate()
        .apply(req)

      contentAsString(result) mustBe ""
      status(result) mustBe 303
      session(result).get("userEmail") mustBe Some(email)
    }
  }
}
