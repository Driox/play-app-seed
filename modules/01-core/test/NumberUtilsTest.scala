import utils.NumberUtils

import org.scalatestplus.play.PlaySpec

class NumberUtilsTest extends PlaySpec {

  // This 4-tuple list represents (percentage: Double, amount: Cents, expected_result: Double, truncated_expected_result: Double)
  val data_factory: List[(Double, Int, Double, Double)] = List[(Double, Int, Double, Double)](
    (25, 50, 12.5, 12),
    (10.2489751, 123456, 12652.97, 12652),
    (33, 100, 33, 33),
    (6.95930407, Integer.MAX_VALUE, 1.4944991685e8, 149449916),
    (7.96920308, Integer.MIN_VALUE, -1.7113733302e8, -171137333),
    (10.3489651, -98765, -10221.16, -10221),
    (6.299370063, 1, 0.06, 0),
    (0.000000000001234, 1, 0, 0),
    (-50, 20, -10, -10),
    (0, 10, 0, 0),
    (50, 0, 0, 0),
    (150, Integer.MAX_VALUE, 3.2212254705e9, -1073741826)
  )

  "NumberUtils" should {
    "successfully compute value of a given percentage from a given input" in {
      data_factory.map {
        case (percentage, amount, expected_result, _) =>
          val result = NumberUtils.percentageValue(percentage, amount)
          result mustBe expected_result
      }
    }

    "successfully compute truncated value of a given percentage from a given input" in {
      data_factory.map {
        case (percentage, amount, _, truncated_expected_result) =>
          val result = NumberUtils.percentageValueTruncated(percentage, amount)
          result mustBe truncated_expected_result
      }
    }
  }

}
