package security.hmac

import play.api.Configuration
import play.api.libs.ws.StandaloneWSRequest
import play.api.mvc.RequestHeader

/**
 * @param key : client api key
 * @param secret : shared secret to sign the authorization header
 * @param prefix : a standard prefix for the authorization header, by default HMAC
 * @param time_windows : validity of the token in minutes
 * @param custom_headers : additional header to be signed
 */
case class HmacSecurityConfig(
  key:            String,
  secret:         String,
  prefix:         String       = "HMAC",
  time_windows:   Int          = 5,
  custom_headers: List[String] = List()
) {
  require(!key.isEmpty, "key can't be empty")
  require(!secret.isEmpty, "secret can't be empty")
}

case class HmacSecurityRequest(
  verb:           String,
  body_hash:      Option[String]         = None,
  content_type:   Option[String]         = None,
  date:           String,
  nonce:          String                 = "",
  custom_headers: List[(String, String)] = List()
) {
  require(!verb.isEmpty, "verb can't be empty")
}

object HmacSecurity {

  def apply(key: String, secret: String, time_windows: Int = 5): HmacCoreSecurity =
    new HmacCoreSecurity(HmacSecurityConfig(
      key          = key,
      secret       = secret,
      time_windows = time_windows
    ))

  def parse_config(configuration: Configuration): HmacSecurityConfig = HmacSecurityConfig(
    configuration.get[String]("hmacsecurity.key"),
    configuration.get[String]("hmacsecurity.secret"),
    configuration.getOptional[String]("hmacsecurity.prefix").getOrElse("HMAC"),
    configuration.getOptional[Int]("hmacsecurity.time_window").getOrElse(10),
    configuration.getOptional[Seq[String]]("hmacsecurity.headers").map(_.toList).getOrElse(List())
  )

  // TODO : implement body in sign
  def wsRequest2HmacRequest(r: StandaloneWSRequest, custom_headers: List[(String, String)]): HmacSecurityRequest =
    HmacSecurityRequest(
      r.method,
      None,
      r.contentType,
      r.headers.get("Date").flatMap(_.headOption).getOrElse(""),
      r.headers.get("nonce").flatMap(_.headOption).getOrElse(""),
      custom_headers
    )

  // TODO : implement body in sign
  def requestHeader2HmacRequest(r: RequestHeader, custom_headers: List[(String, String)]): HmacSecurityRequest =
    HmacSecurityRequest(
      r.method,
      None,
      r.contentType,
      r.headers.get("Date").getOrElse(""),
      r.headers.get("nonce").getOrElse(""),
      custom_headers
    )
}
