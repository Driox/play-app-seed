package utils

import java.text.Normalizer
import java.util.regex.Pattern

object StringUtils {

  private val emailRegex = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r
  def isEmail(maybeEmail: String) = maybeEmail != null && !maybeEmail.trim.isEmpty && emailRegex.findFirstMatchIn(maybeEmail).isDefined

  def stripAccent(s: String): String = {
    val nfdNormalizedString: String = Normalizer.normalize(s, Normalizer.Form.NFD)
    nfdNormalizedString.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
  }

  def toInt(s: String): Option[Int] = {
    try {
      Some(s.toInt)
    } catch {
      case e: Exception => None
    }
  }

  def generateUuid(): String = {
    java.util.UUID.randomUUID().toString
  }
}
