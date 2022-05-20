package test.fixture

import javax.inject._

import models._

import scala.concurrent.ExecutionContext.Implicits.global
import test.global.TestGlobal
import com.google.inject.AbstractModule

import scala.concurrent.Future
import play.api.inject.ApplicationLifecycle
import play.api.Logging

class FixtureModule() extends AbstractModule with TestGlobal {

  override def configure() = {
    bind(classOf[UserData]).asEagerSingleton()
  }
}

@Singleton
class UserData @Inject() (userDao: Users, lifecycle: ApplicationLifecycle) extends TestGlobal with Logging {

  await(setUp())

  lifecycle.addStopHook { () =>
    tearDown()
  }

  def setUp(): Future[_] = {
    logger.debug(s"setup UserData")
    userDao.createTable().flatMap { _ =>
      userDao.create(User(email = "jean.dupont@gmail.com", password = "12345678"))
    }
  }

  def tearDown(): Future[_] = {
    logger.debug(s"tearDown UserData")
    userDao.dropTable()
  }
}
