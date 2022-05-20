package effect.zio.slick

import effect.Fail

import zio.{ Has, ZIO }

package object zioslick {

  type ZioSlickEffect[T] = ZIO[Has[SlickDatabase], Fail, T]

}
