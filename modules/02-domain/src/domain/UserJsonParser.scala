package domain

import domain._

import ai.x.play.json.Encoders._
import ai.x.play.json._
import play.api.libs.json.{ Format, OFormat }
import tagging.AnyTypeclassTaggingCompat

trait UserJsonParser extends AnyTypeclassTaggingCompat {

  implicit val userParser: OFormat[User] = Jsonx.formatCaseClassUseDefaults[User]

  implicit val userStateJsonParser: Format[UserPersistentBehavior.UserState]                = Jsonx.formatCaseClassUseDefaults[UserPersistentBehavior.UserState]
  implicit val userCmdUpdParser: OFormat[UserPersistentBehavior.UserCommand.USER_UPDATE]    = Jsonx.formatCaseClassUseDefaults[UserPersistentBehavior.UserCommand.USER_UPDATE]
  implicit val userCmdCreaParser: OFormat[UserPersistentBehavior.UserCommand.USER_CREATION] = Jsonx.formatCaseClassUseDefaults[UserPersistentBehavior.UserCommand.USER_CREATION]
  implicit val userCmdJsonParser: Format[UserPersistentBehavior.UserCommand]                = Jsonx.formatSealed[UserPersistentBehavior.UserCommand]

  implicit val userEventUpdParser: OFormat[UserPersistentBehavior.UserEvent.USER_CREATED]  = Jsonx.formatCaseClassUseDefaults[UserPersistentBehavior.UserEvent.USER_CREATED]
  implicit val userEventCreaParser: OFormat[UserPersistentBehavior.UserEvent.USER_UPDATED] = Jsonx.formatCaseClassUseDefaults[UserPersistentBehavior.UserEvent.USER_UPDATED]
  implicit val userEventJsonParser: Format[UserPersistentBehavior.UserEvent]               = Jsonx.formatSealed[UserPersistentBehavior.UserEvent]

}
