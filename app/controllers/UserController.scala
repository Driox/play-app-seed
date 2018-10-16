package controllers

import javax.inject._
import models.Users
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext

@Singleton
class UserController @Inject() (val userDao: Users)(implicit ec: ExecutionContext) extends BaseController {

  def changeLang(id: String, lng: String) = Action { implicit request =>
    val json = Json.parse(s"""
     { "lng" : "$lng" }
    """)
    Ok(json)
  }
}
