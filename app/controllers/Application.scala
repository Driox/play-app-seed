package controllers

import javax.inject._

import models.Users

import scala.concurrent.ExecutionContext

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class Application @Inject() (val userDao: Users)(implicit ec: ExecutionContext) extends BaseController {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def home = Action { implicit request =>
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
      Redirect(routes.Application.home).withSession(
        "userEmail" -> user.email
      )
    }
  }
}
