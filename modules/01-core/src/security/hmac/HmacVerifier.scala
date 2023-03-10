package security.hmac

import helpers.sorus.Fail
import play.api.Configuration
import play.api.cache.AsyncCacheApi
import play.api.mvc.RequestHeader
import scalaz.Scalaz._
import scalaz._
import security.hmac.verifier.NonceVerifier

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class HmacVerifier @Inject() (configuration: Configuration, cache: AsyncCacheApi, ec: ExecutionContext) {

  lazy val config: HmacSecurityConfig = HmacSecurity.parse_config(configuration)

  val nonce_verifier: NonceVerifier = new NonceVerifier(cache)(ec)

  def verify(req: RequestHeader)(implicit ec: ExecutionContext): EitherT[Future, Fail, Boolean] = {
    verify(req, config.key, config.secret)
  }

  def verify(
    req:         RequestHeader,
    key:         String,
    secret:      String
  )(implicit ec: ExecutionContext): EitherT[Future, Fail, Boolean] = {
    req.headers.get("Authorization")
      .map { auth =>
        val custom_headers = req.headers.toSimpleMap
          .filter(h => config.custom_headers.exists(_.equalsIgnoreCase(h._1)))
          .toList

        new HmacCoreSecurity(config.copy(key = key, secret = secret))
          .withVerifier(nonce_verifier.verify)
          .verify(auth, HmacSecurity.requestHeader2HmacRequest(req, custom_headers))
      }
      .getOrElse(EitherT.pureLeft[Future, Fail, Boolean](Fail("Can't find Authorization header")))
  }
}
