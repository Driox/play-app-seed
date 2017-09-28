package controllers

import helpers.sorus.{FormatErrorResult, SorusPlay}
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc._

trait BaseController extends InjectedController
    with SorusPlay[Request[_]]
    with FormatErrorResult[Request[_]]
    with I18nSupport {

  implicit def request2lang(implicit request: Request[_]): Lang = {
    request.lang
  }

  def translateError(msg: String)(implicit lang: Lang): String = {
    utils.m(msg)
  }
}
