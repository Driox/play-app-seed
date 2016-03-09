package controllers

import play.api._
import play.api.mvc._

trait BaseController extends Controller {

  def translateError(msg: String): String = {
    utils.m(msg)
  }
}
