package modules

import akka.actor.ActorSystem
import play.api.{ Configuration, Logging }

import javax.inject._
import scala.concurrent.ExecutionContext

/**
 * OnStart code is executed in the constructor of this class
 */
@Singleton
class OnStart @Inject() (
  system: ActorSystem,
  config: Configuration,
  ec:     ExecutionContext
) extends Logging {

  logger.info("--- App started : Plug ---")

}
