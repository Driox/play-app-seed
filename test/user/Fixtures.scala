package user

import global.SetUpFixture
import models.Users
import play.api.{Application, Logger}
import scala.concurrent.ExecutionContext.Implicits.global

object Fixtures extends SetUpFixture {

  def userDao = new Users

  def setUp()(implicit app: Application) = {
    val result = for {
      _ <- userDao.createTable()
    } yield {
      logger.debug("done")
    }

    await(result)
  }

  def tearDown()(implicit app: Application) = {
    await(userDao.dropTable())
    logger.debug("Drop DB")
  }
}
