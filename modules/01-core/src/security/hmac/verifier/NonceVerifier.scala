package security.hmac.verifier

import play.api.cache.AsyncCacheApi
import security.hmac.HmacSecurityRequest

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

class NonceVerifier(cache: AsyncCacheApi)(implicit ec: ExecutionContext) {

  private[this] val nonce_min_size = 1

  def verify(req: HmacSecurityRequest): Future[Boolean] = {
    val nonce = req.nonce
    if(nonce.trim.length < nonce_min_size) {
      Future.successful(false)
    } else {
      verify_nonce(req.nonce)
    }
  }

  private[this] def verify_nonce(nonce: String): Future[Boolean] = {
    cache.get[Int](nonce).map { maybe_in_cache =>
      maybe_in_cache
        .map(_ => false)
        .getOrElse {
          cache.set(nonce, 1, 12 hours)
          true
        }
    }
  }
}
