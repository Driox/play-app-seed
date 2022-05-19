package security.hmac.verifier

import effect.Fail
import play.api.cache.AsyncCacheApi
import security.hmac.HmacSecurityRequest

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import zio._

class NonceVerifier(cache: AsyncCacheApi)(implicit ec: ExecutionContext) {

  private[this] val nonce_min_size = 1

  def verify(req: HmacSecurityRequest): ZIO[Any, Fail, Boolean] = {
    val nonce = req.nonce
    if(nonce.trim.length < nonce_min_size) {
      ZIO.succeed(false)
    } else {
      verify_nonce(req.nonce)
    }
  }

  private[this] def verify_nonce(nonce: String): ZIO[Any, Fail, Boolean] = {
    val rez = cache.get[Int](nonce).map { maybe_in_cache =>
      maybe_in_cache
        .map(_ => false)
        .getOrElse {
          cache.set(nonce, 1, 12 hours)
          true
        }
    }

    ZIO.fromFuture(ec => rez)
      .mapError(t => Fail("Can't compute nonce in HmacVerifier : cache is not available").withEx(t))
  }
}
