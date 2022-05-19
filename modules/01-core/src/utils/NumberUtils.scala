package utils

import com.ibm.icu.util.Currency
import play.api.i18n.Lang

import java.util.Locale
import scala.math.BigDecimal.RoundingMode
import scala.math.BigDecimal.RoundingMode.RoundingMode

object NumberUtils {

  // for instance, when receiving 100.50€ <=> 10050
  // to handle cents, we divide by 100 <=> 100.50
  def amountFromCentimeToDouble(amount: Int): Double = amountFromCentimeToDouble(amount.toLong)

  def amountFromCentimeToDouble(amount: Long): Double =
    (BigDecimal(amount) / BigDecimal(100)).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble

  def amountFromDoubleToCentime(amount: Double): Int =
    (BigDecimal(amount) * BigDecimal(100)).setScale(2, BigDecimal.RoundingMode.HALF_UP).toInt

  def toInt(amount: BigDecimal): Int = amount.setScale(0, BigDecimal.RoundingMode.HALF_EVEN).toInt

  def displayAmountFromCentime(amount: Long, lang: Lang, maybe_currency: Option[Currency] = None): String = {
    maybe_currency
      .map(currency => displayAmountFromCentime(amount, lang, currency))
      .getOrElse(
        displayAmountFromCentime(amount, lang.locale)
      )
  }

  def displayAmountFromCentime(amount: Long, lang: Lang, currency: Currency): String = {
    val formatter = java.text.NumberFormat.getCurrencyInstance(lang.locale)
    formatter.setCurrency(currency.toJavaCurrency())
    formatter.format(amountFromCentimeToDouble(amount))
  }

  def displayAmountFromCentime(amount: Long, locale: Locale): String = {
    val formatter = java.text.NumberFormat.getCurrencyInstance(locale)
    formatter.format(amountFromCentimeToDouble(amount))
  }

  def centsFromDouble(d: Double): Int =
    (d * 100).toInt // Truncate on cents. Used only on final conversion. Not in computation

  def percentageValueTruncated(
    percentage:    Double,
    total_amount:  Int,
    rounding_mode: RoundingMode = RoundingMode.HALF_UP
  ): Int = { BigDecimal(percentageValue(percentage, total_amount)).setScale(2, rounding_mode).toInt }

  def percentageValue(
    percentage:    Double,
    total_amount:  Int,
    rounding_mode: RoundingMode = RoundingMode.HALF_UP
  ): Double = { (BigDecimal(percentage * total_amount) / BigDecimal(100.0)).setScale(2, rounding_mode).toDouble }

}
