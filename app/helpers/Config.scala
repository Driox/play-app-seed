package helpers

import play.api.Play
import play.api.i18n.Lang

/**
 * It's convenient to have access to the config in a global manner
 *
 * Play go away from this kind of things so we wrap it for easy refactoring in the future
 */
object Config {

  def getString(key: String): Option[String] = Play.current.configuration.get[Option[String]](key)

  private[this] val mode = Play.current.environment.mode

  def isDev(): Boolean = (mode == play.api.Mode.Dev)
  def isProd(): Boolean = (mode == play.api.Mode.Prod)
  def isTest(): Boolean = (mode == play.api.Mode.Test)

  val default_lang = Lang("en-US")
}
