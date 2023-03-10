package effect.zio.slick.zioslick

import effect.Fail
import play.api.Logging
import slick.dbio.{ DBIO, StreamingDBIO }
import utils.StringUtils

import zio.interop.reactivestreams._
import zio.stream.ZStream
import zio.{ Has, ZIO }

object ZioSlickEffect extends Logging {
  def apply[T](action: DBIO[T]): ZioSlickEffect[T] = {
    (for {
      slick_db <- ZIO.access[Has[SlickDatabase]](_.get)
      res      <- ZIO.fromFuture(_ => slick_db.database.run(action))
    } yield {
      res
    }).mapError(mapThrowable2Fail)
  }

  def fromStreamingDBIO[T](dbio: StreamingDBIO[_, T]): ZIO[Has[SlickDatabase], Fail, ZStream[Any, Fail, T]] = {
    (for {
      slick_db <- ZIO.access[Has[SlickDatabase]](_.get)
      rr       <- ZIO.effect(slick_db.database.stream(dbio))
    } yield {
      rr
        .toStream(qSize = 16)
        .mapError(mapThrowable2Fail)
    }).mapError(mapThrowable2Fail)
  }

  private[this] def mapThrowable2Fail(t: Throwable): Fail = {
    val msg = s"[${StringUtils.randomAlphanumericString(8)}]Error in slick layer"
    logger.error(msg, t)
    Fail(msg).withEx(t)
  }
}
