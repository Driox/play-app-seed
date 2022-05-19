package controllers

import javax.inject._

import notifier.WelcomeNotifier
import play.api.data._
import play.api.data.Forms._
import models._

import scala.concurrent.ExecutionContext

@Singleton
class AuthenticationController @Inject() (welcomeNotifier: WelcomeNotifier, val userDao: Users)(implicit
  ec:                                                      ExecutionContext
) extends BaseController {

  val credentialsForm = Form(
    mapping(
      "email"    -> email,
      "password" -> nonEmptyText(minLength = 8)
    )(Credentials.apply)(Credentials.unapply)
  )

  def signin() = Action { implicit request =>
    Ok(views.html.security.signin(credentialsForm))
  }

  def register() = Action.async { implicit request =>
    for {
      form <- credentialsForm.bindFromRequest()         ?| (formWithErrors => Ok(views.html.security.signin(formWithErrors)))
      user <- userDao.create(form.email, form.password) ?| (err =>
                Redirect(routes.AuthenticationController.signin()).flashing("error" -> "error.email.exist")
              )
    } yield {
      welcomeNotifier.notify(user)
      Redirect(routes.Application.home()).withSession(
        "userEmail" -> user.email
      )
    }
  }

  def login() = Action { implicit request =>
    Ok(views.html.security.login(credentialsForm))
  }

  def authenticate() = Action.async { implicit request =>
    for {
      form <- credentialsForm.bindFromRequest() ?| (formWithErrors => Ok(views.html.security.login(formWithErrors)))
      user <-
        userDao.findByEmail(form.email) ?| (err =>
          Redirect(routes.AuthenticationController.login()).flashing("error" -> "error.account.authenticate.email")
        )
    } yield {
      Redirect(routes.Application.home()).withSession(
        "userEmail" -> user.email
      )
    }
  }

  def logout() = Action { implicit request =>
    Redirect(routes.Application.home()).withNewSession
  }

  def validate(uuid: String) = Action.async { implicit request =>
    for {
      user <- userDao.findByUuid(uuid) ?| NotFound
      // do something with your user like
      // _ <- userDao.update(user.copy(confirmed = true)) ?| NotFound
    } yield {
      Redirect(routes.Application.home()).withSession(
        "userEmail" -> user.email
      )
    }
  }

  def forgottenPassword() = Action { implicit request =>
    Ok(views.html.security.forgotPassword())
  }
}
