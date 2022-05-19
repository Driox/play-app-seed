package test

import org.scalatest.matchers.{ MatchResult, Matcher }

trait CaseClassComparison {
  def matchTo[A <: Product](right: A): DiffForMatcher[A] = new DiffForMatcher(right)

  class DiffForMatcher[A <: Product](right: A) extends Matcher[A] {
    override def apply(left: A): MatchResult = {

      val left_map  = left.productElementNames.zip(left.productIterator).toMap
      val right_map = right.productElementNames.zip(right.productIterator).toMap
      val diff      = (right_map.toSet diff left_map.toSet)

      val error_msg =
        s"""Matching error:
           |  ${diff.map { case (label, _) =>
          s"$label -> ${left_map.getOrElse(label, "No Value")} \n!=\n ${right_map.getOrElse(label, "No Value")}"
        }.mkString("\n")}
         """.stripMargin
      MatchResult(matches = diff.isEmpty, error_msg, "all good")
    }
  }
}

object CaseClassComparison extends CaseClassComparison
