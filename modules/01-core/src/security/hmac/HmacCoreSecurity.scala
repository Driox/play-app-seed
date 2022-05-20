package security.hmac

import helpers.sorus.Fail
import helpers.sorus.SorusDSL.Sorus
import scalaz.{ -\/, \/, \/-, EitherT }
import utils.{ StringUtils, TimeUtils }

import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime

import scala.concurrent.{ ExecutionContext, Future }
import security.Crypto

import org.slf4j.LoggerFactory

final class HmacCoreSecurity(
  val config:    HmacSecurityConfig,
  val verifiers: List[HmacSecurityRequest => Future[Boolean]] = List()
) extends Sorus
    with HmacCanonizer
    with HmacSecuritySigner
    with HmacSecurityVerifier {

  protected lazy val log = LoggerFactory.getLogger(this.getClass)

  def withVerifier(new_verifiers: (HmacSecurityRequest => Future[Boolean])*): HmacCoreSecurity = {
    new HmacCoreSecurity(config, verifiers ++ new_verifiers)
  }
}

/**
 * inspire by AWS Security
 *
 * cf. http://s3.amazonaws.com/doc/s3-developer-guide/RESTAuthentication.html
 */
private[hmac] trait HmacSecuritySigner { self: HmacCoreSecurity with HmacCanonizer =>

  def authorization_header(req: HmacSecurityRequest): String = {
    val canonize_str = canonize(req)
    buildAuthorizationHeader(canonize_str)
  }

  private[this] def buildAuthorizationHeader(toSign: String): String = {
    if(toSign.isEmpty) {
      log.warn("You try to sign empty data. This won't work")
      s"${config.prefix} ${config.key}:<?>"
    } else {
      buildAuthorizationHeader(config.key, config.secret, toSign)
    }
  }

  private[this] def buildAuthorizationHeader(key: String, secret: String, data: String): String = {
    val toSign: String = secret + key + data
    val secret_bytes   = secret.getBytes(StandardCharsets.UTF_8)

    val hexChars = Crypto.sign(toSign, secret_bytes)

    val signature = StringUtils.toBase64(hexChars.toLowerCase())

    s"${config.prefix} ${config.key}:$signature"
  }
}

private[hmac] trait HmacSecurityVerifier { self: HmacCoreSecurity with HmacSecuritySigner =>

  def verify(authorization: String, req: HmacSecurityRequest)(implicit
    ec:                     ExecutionContext
  ): EitherT[Future, Fail, Boolean] = {
    val custom_result_f: Future[Boolean] = Future.sequence(
      verifiers.map(f => f(req))
    ).map {
      case List() => true
      case l      => l.reduce(_ && _)
    }

    for {
      datetime      <- TimeUtils.parse(req.date)              ?| s"can't parse time ${req.date}"
      _             <- verify_date(datetime)                  ?| s"date ${req.date} is not in the timeframe ${config.time_windows} min"
      _             <- verify_auth_header(authorization, req) ?| s"wrong authorization header"
      custom_result <- custom_result_f                        ?| "Can't compute Security Filter custom verifier"
      _             <- custom_result                          ?| "custom verify ko"
    } yield {
      true
    }
  }

  private[this] def verify_date(date: OffsetDateTime): Boolean = {
    val past = TimeUtils.now.minusMinutes(config.time_windows.toLong)
    val next = TimeUtils.now.plusMinutes(config.time_windows.toLong)

    past.isBefore(date) && next.isAfter(date)
  }

  private[this] def verify_auth_header(
    authorization: String,
    req:           HmacSecurityRequest
  ): Fail \/ Boolean = {
    val auth_header = authorization_header(req)
    (auth_header == authorization) match {
      case true  => \/-(true)
      case false => -\/(Fail(s"authorization header $auth_header is not equal to authorization $authorization"))
    }

  }
}

private[hmac] trait HmacCanonizer { self: HmacCoreSecurity =>

  private[hmac] def canonize(req: HmacSecurityRequest): String = {
    if(req.verb.isEmpty) {
      log.warn(
        "[HmacSecurity]compute a secure header for a request without HTTP verb is useless. You request will usually be rejected."
      )
    }
    if(req.date.isEmpty) {
      log.warn(
        "[HmacSecurity]compute a secure header for a request without 'Date' header is useless. You request will usually be rejected."
      )
    }

    val custom = req.custom_headers
      .filter(_._1.toLowerCase != "content-md5")
      .filter(_._1.toLowerCase != "content-type")
      .filter(_._1.toLowerCase != "date")
      .sortBy(_._1.toLowerCase()).map(_._2).mkString("\n")

    s"""${req.verb}
       |${req.body_hash.getOrElse("")}
       |${req.content_type.getOrElse("application/octet-stream")}
       |${req.date}
       |$custom""".stripMargin
  }
}
