package helpers

import javax.crypto._
import javax.crypto.spec.{ IvParameterSpec, SecretKeySpec }
import play.api.libs.Codecs

import java.security.{ MessageDigest, SecureRandom }

import org.apache.commons.codec.binary.{ Base64, Hex }

/**
 * Cryptographic utilities.
 *
 * These utilities are intended as a convenience, however it is important to read each methods documentation and
 * understand the concepts behind encryption to use this class properly.  Safe encryption is hard, and there is no
 * substitute for an adequate understanding of cryptography.  These methods will not be suitable for all encryption
 * needs.
 *
 * For more information about cryptography, we recommend reading the OWASP Cryptographic Storage Cheatsheet:
 *
 * https://www.owasp.org/index.php/Cryptographic_Storage_Cheat_Sheet
 */
object Crypto {

  /**
   * Exception thrown by the Crypto APIs.
   * @param message The error message.
   * @param throwable The Throwable associated with the exception.
   */
  class CryptoException(val message: String, val throwable: Throwable = null)
    extends RuntimeException(message, throwable)

  private[this] val random                          = new SecureRandom()
  private[this] final val aesTransformation: String = "AES/CTR/NoPadding"
  private[this] final val UTF_8                     = "UTF-8"
  private[this] final val HMAC_SHA1                 = "HmacSHA1"

  /**
   * Signs the given String with HMAC-SHA1 using the given key.
   *
   * By default this uses the platform default JSSE provider.  This can be overridden by defining
   * `play.crypto.provider` in `application.conf`.
   *
   * @param message The message to sign.
   * @param key The private key to sign with.
   * @return A hexadecimal encoded signature.
   */
  def sign(message: String, key: Array[Byte]): String = {
    val mac = Mac.getInstance(HMAC_SHA1)
    mac.init(new SecretKeySpec(key, HMAC_SHA1))
    Codecs.toHexString(mac.doFinal(message.getBytes("utf-8")))
  }

  def sign(message: String, key: String): String = sign(message, key.getBytes(UTF_8))

  /**
   * Sign a token.  This produces a new token, that has this token signed with a nonce.
   *
   * This primarily exists to defeat the BREACH vulnerability, as it allows the token to effectively be random per
   * request, without actually changing the value.
   *
   * @param token The token to sign
   * @return The signed token
   */
  def signToken(token: String, secret: String): String = {
    val nonce  = System.currentTimeMillis()
    val joined = s"$nonce-$token"
    sign(joined, secret) + "-" + joined
  }

  /**
   * Extract a signed token that was signed by [[helpers.Crypto.signToken]].
   *
   * @param token The signed token to extract.
   * @return The verified raw token, or None if the token isn't valid.
   */
  def extractSignedToken(token: String, secret: String): Option[String] = {
    token.split("-", 3) match {
      case Array(signature, nonce, raw) if constantTimeEquals(signature, sign(nonce + "-" + raw, secret)) => Some(raw)
      case _                                                                                              => None
    }
  }

  /**
   * Generate a cryptographically secure token
   */
  def generateToken: String = {
    val bytes = new Array[Byte](12)
    random.nextBytes(bytes)
    new String(Hex.encodeHex(bytes))
  }

  /**
   * Compare two signed tokens
   */
  def compareSignedTokens(tokenA: String, tokenB: String, secret: String): Boolean = {
    (for {
      rawA <- extractSignedToken(tokenA, secret)
      rawB <- extractSignedToken(tokenB, secret)
    } yield constantTimeEquals(rawA, rawB)).getOrElse(false)
  }

  /**
   * Constant time equals method.
   *
   * Given a length that both Strings are equal to, this method will always run in constant time.  This prevents
   * timing attacks.
   */
  def constantTimeEquals(a: String, b: String): Boolean = {
    if(a.length != b.length) {
      false
    } else {
      var equal = 0
      for(i <- 0 until a.length) {
        equal |= a(i) ^ b(i)
      }
      equal == 0
    }
  }

  /**
   * Encrypt a String with the AES encryption standard and the supplied private key.
   *
   * The provider used is by default this uses the platform default JSSE provider.  This can be overridden by defining
   * `play.crypto.provider` in `application.conf`.
   *
   * The transformation algorithm used is the provider specific implementation of the `AES` name.  On Oracles JDK,
   * this is `AES/CTR/NoPadding`.  This algorithm is suitable for small amounts of data, typically less than 32
   * bytes, hence is useful for encrypting credit card numbers, passwords etc.  For larger blocks of data, this
   * algorithm may expose patterns and be vulnerable to repeat attacks.
   *
   * The transformation algorithm can be configured by defining `play.crypto.aes.transformation` in
   * `application.conf`.  Although any cipher transformation algorithm can be selected here, the secret key spec used
   * is always AES, so only AES transformation algorithms will work.
   *
   * @param value The String to encrypt.
   * @param privateKey The key used to encrypt.
   * @return A Base64 encrypted string.
   */
  def encryptAES(value: String, privateKey: String): String = {
    val skeySpec       = secretKeyWithSha256(privateKey, "AES")
    val cipher         = getCipherWithConfiguredProvider(aesTransformation)
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
    val encryptedValue = cipher.doFinal(value.getBytes("utf-8"))
    // return a formatted, versioned encrypted string
    // '2-*' represents an encrypted payload with an IV
    // '1-*' represents an encrypted payload without an IV
    Option(cipher.getIV()) match {
      case Some(iv) => s"2-${Base64.encodeBase64String(iv ++ encryptedValue)}"
      case None     => s"1-${Base64.encodeBase64String(encryptedValue)}"
    }
  }

  /**
   * Generates the SecretKeySpec, given the private key and the algorithm.
   */
  private[this] def secretKeyWithSha256(privateKey: String, algorithm: String) = {
    val messageDigest       = MessageDigest.getInstance("SHA-256")
    messageDigest.update(privateKey.getBytes("utf-8"))
    // max allowed length in bits / (8 bits to a byte)
    val maxAllowedKeyLength = Cipher.getMaxAllowedKeyLength(algorithm) / 8
    val raw                 = messageDigest.digest().slice(0, maxAllowedKeyLength)
    new SecretKeySpec(raw, algorithm)
  }

  /**
   * Gets a Cipher with a configured provider, and a configurable AES transformation method.
   */
  private[this] def getCipherWithConfiguredProvider(transformation: String): Cipher = Cipher.getInstance(transformation)

  /**
   * Decrypt a String with the AES encryption standard.
   *
   * The private key must have a length of 16 bytes.
   *
   * The provider used is by default this uses the platform default JSSE provider.  This can be overridden by defining
   * `play.crypto.provider` in `application.conf`.
   *
   * The transformation used is by default `AES/CTR/NoPadding`.  It can be configured by defining
   * `play.crypto.aes.transformation` in `application.conf`.  Although any cipher transformation algorithm can
   * be selected here, the secret key spec used is always AES, so only AES transformation algorithms will work.
   *
   * @param value An hexadecimal encrypted string.
   * @param privateKey The key used to encrypt.
   * @return The decrypted String.
   */
  def decryptAES(value: String, privateKey: String): String = {
    val seperator = "-"
    val sepIndex  = value.indexOf(seperator)
    if(sepIndex < 0) {
      decryptAESVersion0(value, privateKey)
    } else {
      val version = value.substring(0, sepIndex)
      val data    = value.substring(sepIndex + 1, value.length())
      version match {
        case "1" => {
          decryptAESVersion1(data, privateKey)
        }
        case "2" => {
          decryptAESVersion2(data, privateKey)
        }
        case _   => {
          throw new CryptoException("Unknown version")
        }
      }
    }
  }

  /** Backward compatible AES ECB mode decryption support. */
  private[this] def decryptAESVersion0(value: String, privateKey: String): String = {
    val raw      = privateKey.substring(0, 16).getBytes("utf-8")
    val skeySpec = new SecretKeySpec(raw, "AES")
    val cipher   = getCipherWithConfiguredProvider("AES")
    cipher.init(Cipher.DECRYPT_MODE, skeySpec)
    new String(cipher.doFinal(Codecs.hexStringToByte(value)))
  }

  /** V1 decryption algorithm (No IV). */
  private[this] def decryptAESVersion1(value: String, privateKey: String): String = {
    val data     = Base64.decodeBase64(value)
    val skeySpec = secretKeyWithSha256(privateKey, "AES")
    val cipher   = getCipherWithConfiguredProvider(aesTransformation)
    cipher.init(Cipher.DECRYPT_MODE, skeySpec)
    new String(cipher.doFinal(data), "utf-8")
  }

  /** V2 decryption algorithm (IV present). */
  private[this] def decryptAESVersion2(value: String, privateKey: String): String = {
    val data      = Base64.decodeBase64(value)
    val skeySpec  = secretKeyWithSha256(privateKey, "AES")
    val cipher    = getCipherWithConfiguredProvider(aesTransformation)
    val blockSize = cipher.getBlockSize
    val iv        = data.slice(0, blockSize)
    val payload   = data.slice(blockSize, data.size)
    cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(iv))
    new String(cipher.doFinal(payload), "utf-8")
  }

  def encodeBase64(s: String): String = {
    val b_array = Base64.encodeBase64(s.getBytes(UTF_8))
    new String(b_array, UTF_8)
  }
}
