package modules

import com.google.inject.AbstractModule
import models.dao.DbDriver.{DbProfile, PostgreSqlDriver}
import play.api.{Configuration, Environment}

/**
 * We override this binding in the test to map to h2 db in test and keep postgresql in dev / prod
 */
class DriverModule(environment: Environment, configuration: Configuration) extends AbstractModule {

  def configure(): Unit = {
    bind(classOf[DbProfile])
      .to(classOf[PostgreSqlDriver])
  }
}
