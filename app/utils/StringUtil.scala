package utils

import java.text.Normalizer
import java.util.regex.Pattern
import java.security.SecureRandom
import scala.util.Try

object StringUtils {

  def generateUuid(): String = {
    java.util.UUID.randomUUID().toString
  }

  def deAccent(str: String): String = {
    if (str == null || str.isEmpty) {
      ""
    } else {
      val nfdNormalizedString: String = Normalizer.normalize(str, Normalizer.Form.NFD);
      val pattern: Pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
      pattern.matcher(nfdNormalizedString).replaceAll("");
    }
  }

  /**
   * String validation via regex is dangerous, cf.
   *
   * Regular expressions (regexs) are frequently subject to Denial of Service (DOS) attacks (called ReDOS).
   * This is due to the fact that regex engines may take a large amount of time when analyzing certain strings, depending on how the regex is defined.
   * For example, for the regex: ^(a+)+$, the input "aaaaaaaaaaaaaaaaX" will cause the regex engine to analyze 65536 different paths.[1] Example taken from OWASP references
   *
   * Therefore, it is possible that a single request may cause a large amount of computation on the server side.
   * The problem with this regex, and others like it, is that there are two different ways the same input character can be accepted
   * by the Regex due to the + (or a *) inside the parenthesis, and the + (or a *) outside the parenthesis.
   * The way this is written, either + could consume the character 'a'. To fix this, the regex should be rewritten to
   * eliminate the ambiguity. For example, this could simply be rewritten as: ^a+$, which is presumably what the author
   * meant anyway (any number of a's). Assuming that's what the original regex meant, this new regex can be evaluated quickly,
   * and is not subject to ReDOS.
   *
   *
   * use OWASP regex to be safe : https://www.owasp.org/index.php/OWASP_Validation_Regex_Repository
   */
  private[this] val emailRegex = """^[a-zA-Z0-9+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,7}$""".r
  private[this] val uuidRegex = "(^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$)".r
  private[this] val ipRegex = """^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$""".r

  def isEmail(e: String): Boolean = Try {
    !e.trim.isEmpty && emailRegex.findFirstMatchIn(e).isDefined
  }.getOrElse(false)

  def isUuid(maybeUuid: String): Boolean = Try {
    !maybeUuid.trim.isEmpty && uuidRegex.findFirstMatchIn(maybeUuid).isDefined
  }.getOrElse(false)

  def isIp(maybeIp: String): Boolean = Try {
    !maybeIp.trim.isEmpty && ipRegex.findFirstMatchIn(maybeIp).isDefined
  }.getOrElse(false)

  def stripAccent(s: String): String = {
    val nfdNormalizedString: String = Normalizer.normalize(s, Normalizer.Form.NFD)
    nfdNormalizedString.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
  }

  /**
   * Elegant random strign generation in Scala -> http://www.bindschaedler.com/2012/04/07/elegant-random-string-generation-in-scala/
   */
  //Random Generator
  private[this] val random = new SecureRandom()

  // Generate a random string of length n from the given alphabet
  private[this] def randomString(alphabet: String)(n: Int): String = {
    Stream.continually(random.nextInt(alphabet.size)).map(alphabet).take(n).mkString
  }

  // Generate a random alphabnumeric string of length n
  def randomAlphanumericString(n: Int): String = {
    randomString("abcdefghijklmnopqrstuvwxyz0123456789")(n)
  }
}
