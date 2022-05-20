package effect.zio.sorus

import effect.Fail

import scala.concurrent.Future

import zio._

/**
 * We need to pass Future by name to let ZIO run it
 */
class ZioFuture[A](future: => Future[A]) {
  def ?|(err_msg: String): ZIO[Any, Fail, A] =
    ZIO.fromFuture(_ => future)
      .mapError(throwable => Fail(err_msg).withEx(throwable))
}

class ZioOption[A](opt: Option[A]) {
  def ?|(err_msg: String): ZIO[Any, Fail, A] =
    ZIO.fromOption(opt)
      .mapError(_ => Fail(err_msg))
}

class ZioEither[E, A](either: Either[E, A]) {
  def ?|(): ZIO[Any, Fail, A] =
    ZIO.fromEither(either)
      .mapError(err => Fail(err.toString))

  def ?|(err_msg: String): ZIO[Any, Fail, A] =
    ZIO.fromEither(either)
      .mapError(err => Fail(err.toString).withEx(err_msg))
}

trait ZioExtension {
  implicit def future2Zio[A](future:    => Future[A]) = new ZioFuture(future)
  implicit def option2Zio[A](option:    Option[A])    = new ZioOption(option)
  implicit def either2Zio[E, A](either: Either[E, A]) = new ZioEither(either)
}

object ZioExtension extends ZioExtension
