package effect.zio

import _root_.zio._
import _root_.zio.blocking.Blocking
import _root_.zio.clock.Clock
import _root_.zio.console.Console
import _root_.zio.random.Random
import _root_.zio.system.System

/**
 * This is private in the lib but we need it
 */
object PlatformAppSpecific {
  type ZEnv = Clock with Console with System with Random with Blocking

  object ZEnv {

    object Services {
      val live: ZEnv =
        Has.allOf[Clock.Service, Console.Service, System.Service, Random.Service, Blocking.Service](
          Clock.Service.live,
          Console.Service.live,
          System.Service.live,
          Random.Service.live,
          Blocking.Service.live
        )
    }

    val any: ZLayer[ZEnv, Nothing, ZEnv] = ZLayer.requires[ZEnv]

    val live: ZLayer[Any, Nothing, ZEnv] =
      Clock.live ++ Console.live ++ System.live ++ Random.live ++ Blocking.live
  }
}
