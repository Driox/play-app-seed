package modules

import play.api.ApplicationLoader
import play.api.Configuration
import play.api.inject._
import play.api.inject.guice._
import play.api.ApplicationLoader.Context
import com.typesafe.config.ConfigFactory
import play.api.Mode._
import play.Logger

class CustomApplicationLoader extends GuiceApplicationLoader {

  override protected def builder(context: Context): GuiceApplicationBuilder = {
    val builder = initialBuilder.in(context.environment).overrides(overrides(context): _*)
    context.environment.mode match {
      case Prod => {
        // start mode
        val prodConf = Configuration(ConfigFactory.load("prod.conf"))
        builder.loadConfig(prodConf ++ context.initialConfiguration)
      }
      case Dev => {
        Logger.error("*** Custom Loader DEV****")
        // run mode
        val devConf = Configuration(ConfigFactory.load("dev.conf"))
        builder.loadConfig(devConf ++ context.initialConfiguration)
      }
      case Test => {
        Logger.error("*** Custom Loader TEST ****")
        // test mode
        val testConf = Configuration(ConfigFactory.load("test.conf"))
        builder.loadConfig(testConf ++ context.initialConfiguration)
      }
    }
  }
}
