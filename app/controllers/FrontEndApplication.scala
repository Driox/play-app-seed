package controllers

import javax.inject._
import models.Users
import play.api.Environment
import play.twirl.api.Html
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext
import utils.FileUtils._

@Singleton
class FrontEndApplication @Inject() (val userDao: Users, env: Environment)(implicit ec: ExecutionContext) extends BaseController {

  def redirectToApp() = Action {
    Redirect(routes.FrontEndApplication.app(""))
  }

  def app(path: String = "") = Action {
    val html = env.getFile("public/react/index.html")
    val headers = views.html.app.headers().toString
    val initial_data = Json.parse(
      s""" {
        "user" : { "email" : "jean.dupont@gmail.com", "id": "1234" },
        "lang" : "en",
        "is_prod" : ${env.mode == play.api.Mode.Prod}
        } """
    )
    val html_content = html.read()
      .replaceAll("""<meta content="__SERVER_HEADER__">""", headers)
      .replaceAll("__SERVER_DATA__", initial_data.toString())

    Ok(Html.apply(html_content))
  }
}
