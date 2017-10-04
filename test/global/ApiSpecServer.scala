package global

import org.scalatest.TestData
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results
import test.fixture.FixtureModule

abstract class ApiSpecServer extends PlaySpec with GuiceOneAppPerTest with Results with ScalaFutures with IntegrationPatience {

  val currentVersion = 1

  // Override newAppForTest if you need an Application with other than
  // default parameters.
  override def newAppForTest(td: TestData) = new GuiceApplicationBuilder()
    .configure(
      Map(
        "slick.dbs.default.profile" -> "slick.jdbc.H2Profile$",
        "slick.dbs.default.db.driver" -> "org.h2.Driver",
        "slick.dbs.default.db.url" -> "jdbc:h2:mem:play;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE",
        "slick.dbs.default.db.user" -> "sa",
        "slick.dbs.default.db.password" -> "",
        "slick.dbs.default.db.connectionPool" -> "disabled",
        "slick.dbs.default.db.keepAliveConnection" -> "true",

        "play.evolutions.enabled" -> "false",
        "play.evolutions.autoApply" -> "false",
        "play.evolutions.autocommit" -> "false",

        "ehcacheplugin" -> "disabled"
      )
    )
    .bindings(new FixtureModule)
    .build()
}
