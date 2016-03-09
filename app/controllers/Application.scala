package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import models._
import models.dao.DaoAware
import api.controllers.ActionDSL._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class Application @Inject() extends BaseController with MonadicActions with DaoAware {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def ping() = Action {
    Ok("Ok")
  }

  def unsubscribe(uuid: String) = Action.async { implicit request =>
    for {
      user <- userDao.findByUuid(uuid) ?| NotFound
      // Do something with your user like 
      // userReloaded <- userDao.update(user.copy(allow_email = false)) ?| NotFound
    } yield {
      Redirect(controllers.routes.Application.index).withSession(
        "userEmail" -> user.email
      )
    }
  }
}
