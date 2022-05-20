import utils.ConverterUtils

import org.scalatestplus.play.PlaySpec

import com.ibm.icu.util.Currency

class ConverterUtilsTest extends PlaySpec {

  "ConverterUtils" should {
    "convert string to long" in {
      ConverterUtils.stringToLong("87687687687") mustBe Some(87687687687L)
      ConverterUtils.stringToLong("-12") mustBe Some(-12L)
      ConverterUtils.stringToLong("-1ddd2") mustBe None
    }
    "convert string to int" in {

      ConverterUtils.stringToLong("87687") mustBe Some(87687)
      ConverterUtils.stringToLong("-12") mustBe Some(-12)
      ConverterUtils.stringToLong("-1ddd2") mustBe None
    }
    "convert string to currency" in {
      ConverterUtils.stringToCurrency("EUR") mustBe Some(Currency.getInstance("EUR"))
      ConverterUtils.stringToCurrency("USD") mustBe Some(Currency.getInstance("USD"))
      ConverterUtils.stringToCurrency("slkfdjlsdkfj") mustBe None
    }
  }
}
