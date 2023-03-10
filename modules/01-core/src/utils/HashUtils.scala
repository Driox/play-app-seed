package utils

import java.nio.charset.StandardCharsets
import scala.util.Try

import org.mindrot.jbcrypt.BCrypt

object HashUtils {

  /**
   * Create an encrypted password from a clear String
   */
  def createPassword(clearString: String): String = {
    BCrypt.hashpw(clearString, BCrypt.gensalt())
  }

  /**
   * Method to check if entered user password is the same as the one that is
   * stored (encrypted) in the database.
   */
  def checkPassword(candidate: String, encryptedPassword: String): Boolean = Try {
    BCrypt.checkpw(candidate, encryptedPassword)
  }.getOrElse(false)

  /**
   * doc for %02x : https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html#syntax
   *  - 0 : 0 padded
   *  - 2 : take 2 bytes
   *  - x : The result is formatted as a hexadecimal integer
   *
   * .map(0xFF & _) : this part is to ensure all element in the array are converted to byte.
   * On the JVM, you can have non homogeneous array at runtime
   */
  private[this] def byte2hex(byte: Array[Byte]): String =
    byte.map(0xff & _).map("%02x".format(_)).foldLeft("")(_ + _).toUpperCase

  def sha512Hash(text: String): String =
    byte2hex(java.security.MessageDigest.getInstance("SHA-512").digest(text.getBytes(StandardCharsets.UTF_8)))
}
