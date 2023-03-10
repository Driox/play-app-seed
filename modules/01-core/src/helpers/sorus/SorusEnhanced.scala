package helpers.sorus

import helpers.sorus.SorusDSL._
import scalaz._
import scalaz.syntax.either._

import scala.concurrent.Future
import scala.util.control.NonFatal

import org.slf4j.LoggerFactory

trait SorusEnhanced extends Sorus {

  private[this] val logger = LoggerFactory.getLogger("helpers.sorus.SorusEnhanced")

  override implicit def fDisjunctionToStepOps[A, B](fDisjunction: Future[B \/ A]): StepOpsWithDefaultError[A, B] =
    new StepOpsEnhanced[A, B] {
      override def orFailWith(failureHandler: (B) => Fail) =
        EitherT[Future, Fail, A] {
          fDisjunction
            .map(_.leftMap(failureHandler))(executionContext)
            .recover(log(x => (new Fail("Unexpected error in Future from FDisjunction").withEx(x)).left))(
              executionContext
            )
        }
    }

  override implicit def disjunctionToStepOps[A, B](disjunction: B \/ A): StepOpsWithDefaultError[A, B] =
    new StepOpsEnhanced[A, B] {
      override def orFailWith(failureHandler: (B) => Fail) =
        EitherT[Future, Fail, A](Future.successful(disjunction.leftMap(failureHandler)))
    }

  override implicit def eitherToStepOps[A, B](either: Either[B, A]): StepOpsWithDefaultError[A, B] =
    new StepOpsEnhanced[A, B] {
      override def orFailWith(failureHandler: (B) => Fail) =
        EitherT[Future, Fail, A](Future.successful(either.fold(failureHandler andThen \/.left, \/.right)))
    }

  override implicit def fEitherToStepOps[A, B](fEither: Future[Either[B, A]]): StepOpsWithDefaultError[A, B] =
    new StepOpsEnhanced[A, B] {
      override def orFailWith(failureHandler: (B) => Fail) = EitherT[Future, Fail, A] {
        fEither
          .map(_.fold(failureHandler andThen \/.left, \/.right))(executionContext)
          .recover(log(x => (new Fail("Unexpected error in Future from FEither").withEx(x)).left))(executionContext)
      }
    }

  private[this] def log[A](f: Throwable => Fail \/ A): PartialFunction[Throwable, Fail \/ A] = {
    case NonFatal(t) => {
      logger.error("Unexpected error in Future", t)
      f(t)
    }
  }
}

trait StepOpsEnhanced[A, B] extends StepOpsWithDefaultError[A, B] {
  override def ?|(failureThunk: => String): Step[A] = orFailWith {
    case err: Throwable   => new Fail(failureThunk).withEx(err)
    case fail: Fail       => new Fail(failureThunk).withEx(fail)
    case err: ErrorResult => ErrorHandler.errorResult2Fail(err)
    case b                => new Fail(b.toString).withEx(failureThunk)
  }

  override def ?|(empty: Unit): Step[A] = orFailWith {
    case err: Throwable   => new Fail("Unexpected exception").withEx(err)
    case fail: Fail       => fail
    case err: ErrorResult => ErrorHandler.errorResult2Fail(err)
    case b                => new Fail(b.toString)
  }
}
