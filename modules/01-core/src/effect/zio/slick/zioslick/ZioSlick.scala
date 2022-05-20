package effect.zio.slick.zioslick

import effect.Fail
import effect.zio.sorus.ZioSorus
import slick.dbio.{ DBIO, StreamingDBIO }

import zio._
import zio.stream.ZStream

trait ZioSlick extends ZioSorus {

  def db_layer: SlickDatabase

  def DBIO2ZioSlick[T](dbio: DBIO[T]): ZioSlickEffect[T] = ZioSlickEffect[T](dbio)

  implicit def dbio2ZStream[T](db_stream: StreamingDBIO[_, T]): ZIO[Any, Fail, ZStream[Any, Fail, T]] = {
    ZioSlickEffect.fromStreamingDBIO(db_stream).provide(Has(db_layer))
  }

  implicit def dbio2Zio[T](dbio: DBIO[T]): ZIO[Any, Fail, T] = {
    DBIO2ZioSlick(dbio).provide(Has(db_layer))
  }

  implicit def dbio2ZioFail[T](dbio: DBIO[T]): ZioFail[Any, T] = {
    val effect = DBIO2ZioSlick(dbio).provide(Has(db_layer))
    new ZioFail(effect)
  }
}
