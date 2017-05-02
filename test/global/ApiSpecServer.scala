package global

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play._
import play.api.GlobalSettings
import play.api.mvc.Results
import play.api.test._

abstract class ApiSpecServer extends PlaySpec with OneServerPerTest with Results with ScalaFutures with IntegrationPatience {

  val currentVersion = 1

  protected def getFakeApp(global: GlobalSettings): FakeApplication = {
    FakeApplication(
      additionalConfiguration = Map(
        "slick.dbs.default.driver" -> "slick.driver.H2Driver$",
        "slick.dbs.default.db.driver" -> "org.h2.Driver",
        "slick.dbs.default.db.url" -> "jdbc:h2:mem:play;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE",
        "slick.dbs.default.db.user" -> "sa",
        "slick.dbs.default.db.password" -> "",
        "slick.dbs.default.db.connectionPool" -> "disabled",
        "slick.dbs.default.db.keepAliveConnection" -> "true",

        "play.evolutions.enabled" -> "false",
        "play.evolutions.autoApply" -> "false",
        "play.evolutions.autocommit" -> "false"
      ),
      withGlobal = Some(global)
    )
  }
}
