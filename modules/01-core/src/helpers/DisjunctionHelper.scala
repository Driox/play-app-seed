package helpers

import helpers.sorus.Fail
import play.api.libs.json._
import scalaz.{ -\/, \/, \/- }

class JsResultEnhanced[A](jsResult: JsResult[A]) {
  def |>(error_message: String): Fail \/ A = {
    jsResult.fold(
      jsError => -\/(Fail(jsError).withEx(error_message)),
      result => \/-(result)
    )
  }
}

class OptionEnhanced[A](option: Option[A]) {
  def |>(error_message: String): Fail \/ A = option.map(\/-(_)).getOrElse(-\/(Fail(error_message)))
}

class BooleanEnhanced(boolean: Boolean) {
  def |>(error_message: String): Fail \/ Boolean = boolean match {
    case true  => \/-(true)
    case false => -\/(Fail(error_message))
  }
}

class EitherEnhanced[A](either: Either[Fail, A]) {
  def |>(error_message: String): Fail \/ A = either match {
    case Right(success) => \/-(success)
    case Left(fail)     => -\/(fail.withEx(error_message))
  }

  def |>(no_error_message: Unit): Fail \/ A = either match {
    case Right(success) => \/-(success)
    case Left(fail)     => -\/(fail)
  }
}

class DisjunctionEnhanced[A](disjunction: Fail \/ A) {
  def |>(error_message: String): Fail \/ A = disjunction match {
    case -\/(fail) => -\/(fail.withEx(error_message))
    case _         => disjunction
  }
}

trait DisjunctionHelper {
  implicit def optionToDisjunction[A](option: Option[A]): OptionEnhanced[A]         = new OptionEnhanced(option)
  implicit def booleanToDisjunction(boolean: Boolean): BooleanEnhanced              = new BooleanEnhanced(boolean)
  implicit def eitherToDisjunction[A](either: Either[Fail, A]): EitherEnhanced[A]   = new EitherEnhanced(either)
  implicit def jsResultToDisjunction[A](jsResult: JsResult[A]): JsResultEnhanced[A] = new JsResultEnhanced(jsResult)
  implicit def disjunctionToDisjunction[A](disj: Fail \/ A): DisjunctionEnhanced[A] = new DisjunctionEnhanced(disj)

}
object DisjunctionHelper extends DisjunctionHelper
