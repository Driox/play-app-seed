package effect.zio.play

import effect.Fail
import play.api.Logging
import play.api.mvc._

import scala.util.control.NonFatal

import zio.{ Runtime, ZIO }

class ZioActionBuilderOps[+R[_], B](val actionBuilder: ActionBuilder[R, B]) extends AnyVal {
  def zio[Config](zioActionBody: R[B] => ZIO[Config, Fail, Result])(implicit
    runtime:                     Runtime[Config]
  ): Action[B] = {
    ZioActionBuilder.zio[Config, R, B](actionBuilder)(zioActionBody)
  }

  def zio[Config, A](
    bp:               BodyParser[A]
  )(
    zioActionBody:    R[A] => ZIO[Config, Fail, Result]
  )(implicit runtime: Runtime[Config]): Action[A] = {
    ZioActionBuilder.zio[Config, A, R, B](actionBuilder)(bp)(zioActionBody)
  }
}

object ZioActionBuilder extends Logging {

  def zio[Config, R[_], B](
    actionBuilder: ActionBuilder[R, B]
  )(zioActionBody: R[B] => ZIO[Config, Fail, Result])(implicit runtime: Runtime[Config]): Action[B] =
    actionBuilder.async { request =>
      runtime.unsafeRunToFuture(
        appEffectToResult(zioActionBody(request))
      )
    }

  def zio[Config, A, R[_], B](
    actionBuilder:    ActionBuilder[R, B]
  )(
    bp:               BodyParser[A]
  )(
    zioActionBody:    R[A] => ZIO[Config, Fail, Result]
  )(implicit runtime: Runtime[Config]): Action[A] = actionBuilder(bp).async { request =>
    val result = runtime.unsafeRunToFuture(
      appEffectToResult(zioActionBody(request))
    )

    result.recover {
      case NonFatal(throwable) => {
        fail2result(Fail("Unknown exception during execution").withEx(throwable))
      }
    }(scala.concurrent.ExecutionContext.global)

    result
  }

  private[this] def appEffectToResult[Config, E](effect: ZIO[Config, Fail, Result]): ZIO[Config, Throwable, Result] = {
    effect
      .either
      .map {
        case Right(result) => Right(result)
        case Left(fail)    => {
          fail
            .getRootException()
            .map {
              case NonFatal(_) => Right(fail2result(fail))
              case fatal       => Left(fatal)
            }.getOrElse {
              Right(fail2result(fail))
            }
        }
      }
      .absolve
  }

  private[this] def fail2result(fail: Fail): Result = {
    fail match {
      case fail_with_result: FailWithResult => fail_with_result.result
      case fail: Fail                       => Results.BadRequest(ZioPlayHelper.fail2json(fail))
    }
  }
}
