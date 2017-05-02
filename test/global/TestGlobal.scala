package global

import org.joda.time.DateTime
import play.api.{Application, GlobalSettings, Logger}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._

trait TestLogger {
  protected final val logger = Logger("test.global.TestLogger")
}

trait SetUpFixture extends TestLogger {
  def setUp()(implicit app: Application)
  def tearDown()(implicit app: Application)
  def await[A](f: Future[A]): A = TestGlobal.await(f)
  def applyFunction[A](l: List[A], f: A => Future[Option[A]]): List[A] = {
    val as: List[A] = List()
    l.foldLeft(as)((x, u) => (await(f(u)) ++ x).toList)
  }
}

class EmptyFixture extends SetUpFixture {
  override def setUp()(implicit app: Application) = {}
  override def tearDown()(implicit app: Application) = {}
}

class TestGlobal()(implicit fixture: SetUpFixture) extends GlobalSettings with TestLogger {

  // put here common setup / tear down for all your test
}

object TestGlobal {
  def await[A](f: Future[A]): A = Await.result(f, 10 seconds)
  def await[A](f: Seq[Future[A]]): Seq[A] = await(Future.sequence(f))
}
