package utils

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object LangUtils {

  def productToQueryString(product: Product): List[(String, String)] = {
    val result = product.getClass.getDeclaredFields.map(_.getName) // all field names
      .zip(product.productIterator.to(List)).toMap

    val date_time_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")

    result.view.mapValues {
      case Some(x) => x
      case None    => ""
      case x: Any  => x
    }.mapValues {
      case d: OffsetDateTime => d.format(date_time_formatter)
      case v: Any            => v.toString
    }.filter { case (_, v) => v != "" }.map { case (k, v) => (k, v) }.toList
  }
}
