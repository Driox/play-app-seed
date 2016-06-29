package helpers

import models._
import play.twirl.api.Html

class IfTag(condition: Boolean, content: => Html) extends scala.xml.NodeSeq {

  def theSeq = Nil // just ignore, required by NodeSeq

  override def toString = if (condition) content.toString else ""

  def orElse(failed: => Html) = if (condition) content else failed
}

object CustomTag {

  /**
   * Usage :
   *  @isAuthorized(user){
   *    Welcome, @user.name!
   *  }.orElse{
   *    Nothing to see here...
   *  }
   */
  def isAuthorized(user: User)(body: => Html) = new IfTag(user.isAuthorized, body)

}
