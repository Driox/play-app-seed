package effect.zio

import effect.Fail

import zio.ZIO

package object sorus {

  type Sorus[R, A] = ZIO[R, Fail, A]

}
