package test.global

import play.api.Logging
import play.api.mvc.InjectedController
import play.api.test.Helpers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

trait TestGlobal extends Logging {
  def await[A](f: Future[A]): A           = Await.result(f, 10 seconds)
  def await[A](f: Seq[Future[A]]): Seq[A] = await(Future.sequence(f))
}

/**
 * usefull for ctrl testing
 */
trait CtrlHelper {
  def stubify[C <: InjectedController](controller: C): C = {
    controller.setControllerComponents(Helpers.stubControllerComponents())
    controller
  }
}
