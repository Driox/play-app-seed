package utils

object NumberUtils {

  def validePrecision(value: Double, precision: Int) = {
    val valueS = value.toString
    if (valueS.contains(".")) valueS.substring(valueS.indexOf(".") + 1).length <= precision else true
  }

  // in amount with receive for 100.50€ = 10050
  // for handle centime we divide him by 100 = 100.50
  def amountFromCentimeToDouble(amount: Int): Double = (BigDecimal(amount) / BigDecimal(100)).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble

  def amountFromDoubleToCentime(amount: Double): Int = (BigDecimal(amount) * BigDecimal(100)).setScale(2, BigDecimal.RoundingMode.HALF_UP).toInt
}
