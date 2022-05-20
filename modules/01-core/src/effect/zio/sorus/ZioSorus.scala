package effect.zio.sorus

import effect.Fail

import zio.ZIO

/**
 * This allow the stacking of error's messages in the Fail
 */
trait ZioSorus extends ZioExtension {

  class ZioFail[R, A](effect: Sorus[R, A]) {
    def ?|(err_msg:   String): Sorus[R, A]       = effect.mapError(fail => new Fail(err_msg).withEx(fail))
    def ?|(new_fail:  Fail): Sorus[R, A]         = effect.mapError(fail => new_fail.withEx(fail))
    def ?|(fail_func: Fail => Fail): Sorus[R, A] = effect.mapError(fail => fail_func(fail).withEx(fail))
    def ?|(err:       Throwable): Sorus[R, A]    = effect.mapError(fail => fail.withEx(err))
  }

  implicit def zio_with_fail[R, E, A](effect: ZIO[R, E, A]): ZioFail[R, A] = {
    val error_as_fail = effect.mapError {
      case fail: Fail   => fail
      case s: String    => Fail(s)
      case t: Throwable => Fail("Error during execution").withEx(t)
      case x            => Fail(x.toString)
    }
    new ZioFail[R, A](error_as_fail)
  }
}
