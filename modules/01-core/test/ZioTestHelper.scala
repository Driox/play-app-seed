package test

import effect.Fail
import effect.zio.PlatformAppSpecific
import play.api.Logging

import zio._

trait ZioTestHelper extends Logging { self: AppSpec =>

  val default_layer = PlatformAppSpecific.ZEnv.live

  def run[A](effect: ZIO[Any, Fail, A]): Either[Fail, A] = Runtime.default.unsafeRun(effect.either)

  def runInSuccess[A](effect: ZIO[Any, Fail, A]): A = {
    val maybeResult = Runtime.default.unsafeRun(effect.either)
    maybeResult.swap.map(fail => logger.error(s"Error for test :\n  ${fail.userMessage()}\n"))
    maybeResult.isRight mustBe true
    maybeResult.toOption.get
  }

}
