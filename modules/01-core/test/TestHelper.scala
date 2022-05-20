package core

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

object TestHelper extends TestHelper
trait TestHelper {

  def await[A](f: Future[A]): A = Await.result(f, 10 seconds)

}
