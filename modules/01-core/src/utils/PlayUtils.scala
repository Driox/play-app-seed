package utils

import play.api.Configuration
import play.api.http.MediaRange
import play.api.mvc.RequestHeader

object PlayUtils {

  def schema(configuration: Configuration): String = {
    if(configuration.get[Boolean]("application.is.secure")) "https" else "http"
  }

  def absolute_url(configuration: Configuration, relative_url: String): String = {
    if(relative_url.startsWith("http")) {
      relative_url
    } else {
      val host = configuration.get[String]("application.host")
      s"${schema(configuration)}://$host$relative_url"
    }
  }

  private[this] val supportedTypes: Seq[String]    = List("application/json", "text/html")
  def is_json_req(request: RequestHeader): Boolean = {
    MediaRange.preferred(request.acceptedTypes, supportedTypes).map(_ == "application/json").getOrElse(false)
  }
}
