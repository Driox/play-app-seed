package controllers

import api.controllers.ActionDSL._
import notifier.WelcomeNotifier
import play.api._
import play.api.Play.current
import play.api.mvc._
import play.api.data._
import play.api.data.validation._
import play.api.data.Forms._
import play.api.libs.json._

import models._
import models.dao.DaoAware

import api.controllers.{ApiAction, ApiController}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class AuthenticationController extends BaseController with MonadicActions with DaoAware {

  val credentialsForm = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText(minLength = 8)
    )(Credentials.apply)(Credentials.unapply)
  )

  def signin = Action { implicit request =>
    Ok(views.html.security.signin(credentialsForm))
  }

  def register = Action.async { implicit request =>
    for {
      form <- credentialsForm.bindFromRequest ?| (formWithErrors => Ok(views.html.security.signin(formWithErrors)))
      user <- userDao.create(form.email, form.password) ?| (err => Redirect(controllers.routes.AuthenticationController.signin()).flashing("error" -> "error.email.exist"))
    } yield {
      WelcomeNotifier.notify(user)
      Redirect(controllers.routes.Application.index).withSession(
        "userEmail" -> user.email
      )
    }
  }

  def login = Action { implicit request =>
    Ok(views.html.security.login(credentialsForm))
  }

  def authenticate = Action.async { implicit request =>
    for {
      form <- credentialsForm.bindFromRequest ?| (formWithErrors => Ok(views.html.security.login(formWithErrors)))
      user <- userDao.findByEmail(form.email) ?| (err => Redirect(controllers.routes.AuthenticationController.login()).flashing("error" -> "error.account.authenticate.email"))
    } yield {
      Redirect(controllers.routes.Application.index).withSession(
        "userEmail" -> user.email
      )
    }
  }

  def logout = Action { implicit request =>
    Redirect(controllers.routes.Application.index()).withNewSession
  }

  def validate(uuid: String) = Action.async { implicit request =>
    for {
      user <- userDao.findByUuid(uuid) ?| NotFound
      // do something with your user like 
      // _ <- userDao.update(user.copy(confirmed = true)) ?| NotFound
    } yield {
      Redirect(controllers.routes.Application.index).withSession(
        "userEmail" -> user.email
      )
    }
  }

  def forgottenPassword = Action { implicit request =>
    Ok(views.html.security.forgotPassword())
  }
}
