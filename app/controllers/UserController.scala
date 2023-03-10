package controllers

import models.Users
import play.api.libs.json.Json

import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class UserController @Inject() (val userDao: Users)(implicit ec: ExecutionContext) extends BaseController {

  def changeLang(id: String, lng: String) = Action { _ =>
    val json = Json.parse(s"""
     { "lng" : "$lng" }
    """)
    Ok(json)
  }
}
