package global

import user.Fixtures

import scala.concurrent.Future
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeApplication
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.scalatest.TestData

class ApiTest extends ApiSpecServer with TestUtils {

  implicit override def newAppForTest(td: TestData): FakeApplication = getFakeApp(new TestGlobal()(Fixtures))

  "The application " should {

    "return OK on ping" in {
      val result: Future[Result] = (new controllers.Application()).ping().apply(FakeRequest())
      val bodyText: String = contentAsString(result)
      bodyText mustBe "Ok"
    }
  }
}
