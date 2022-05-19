package modules

import play.api.ApplicationLoader.Context
import play.api.Mode._
import play.api.inject.guice._
import play.api.{ Configuration, Logging }

import com.typesafe.config.ConfigFactory

class CustomApplicationLoader extends GuiceApplicationLoader with Logging {

  override protected def builder(context: Context): GuiceApplicationBuilder = {
    val builder = initialBuilder.in(context.environment).overrides(overrides(context): _*)
    context.environment.mode match {
      case Prod => {
        // start mode
        val prodConf = Configuration(ConfigFactory.load("prod.conf"))
        builder.loadConfig(prodConf withFallback context.initialConfiguration)
      }
      case Dev  => {
        logger.error("*** Custom Loader DEV****")
        // run mode
        val devConf = Configuration(ConfigFactory.load("dev.conf"))
        builder.loadConfig(devConf withFallback context.initialConfiguration)
      }
      case Test => {
        logger.error("*** Custom Loader TEST ****")
        // test mode
        val testConf = Configuration(ConfigFactory.load("test.conf"))
        builder.loadConfig(testConf withFallback context.initialConfiguration)
      }
    }
  }
}
