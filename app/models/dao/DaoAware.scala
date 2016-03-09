package models.dao

import models._

trait DaoAware {
  lazy val userDao = new Users()
}