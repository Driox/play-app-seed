package test

import helpers.sorus.Fail
import scalaz.{ -\/, \/, \/- }

import org.scalatest.matchers.{ MatchResult, Matcher }

trait FailChecker {
  def beRight(): RightFailCheckerMatcher = new RightFailCheckerMatcher()
  def beLeft(top_message: String): LeftFailCheckerMatcher = new LeftFailCheckerMatcher(top_message)

  class RightFailCheckerMatcher() extends Matcher[Fail \/ _] {
    override def apply(left: Fail \/ _): MatchResult = {
      val error_msg =
        s"""Result is in failure :
           |  ${left.leftMap(_.userMessage()).swap.getOrElse("No fail message")}
         """.stripMargin
      MatchResult(matches = left.isRight, error_msg, "all good")
    }
  }

  class LeftFailCheckerMatcher(top_message: String) extends Matcher[Fail \/ _] {
    override def apply(result: Fail \/ _): MatchResult = {

      val is_a_match = result
        .leftMap(fail => fail.userMessages().headOption.map(_ == top_message).getOrElse(false))
        .map(_ => false)
        .merge

      val error_msg = result match {
        case \/-(_)    => s"Result is success instead of failure with message $top_message"
        case -\/(fail) => s"""Result fail with message
                             | - ${fail.userMessages().headOption.getOrElse("`no top message`")}
                             |instead of
                             | - $top_message""".stripMargin
      }
      MatchResult(matches = is_a_match, error_msg, "all good")
    }
  }
}

object FailChecker extends FailChecker
