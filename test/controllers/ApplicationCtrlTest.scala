package controllers

import models.Users
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future
import org.scalatestplus.play._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import test.global.CtrlHelper
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * This is a unit test of a ctrl, no db involved
 */
class ApplicationCtrlTest extends PlaySpec with Results with MockitoSugar with CtrlHelper {

  "Application ctrl" should {
    "return Ok on ping" in {

      val userRepository = mock[Users]

      val controller = stubify(new Application(userRepository))
      val result: Future[Result] = controller.ping().apply(FakeRequest())
      val bodyText: String = contentAsString(result)
      bodyText mustBe "Ok"
    }
  }
}
