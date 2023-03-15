package domain

import event.EventSourcedBehavior
import helpers.sorus.Fail
import play.api.libs.json._
import scalaz.{ -\/, \/, \/- }
import tagged.Tags._
import utils.json.JsonSerializable

object UserPersistentBehavior extends UserJsonParser {

  // TODO : check how to remove JsonSerializable from here
  sealed trait UserCommand extends Product with Serializable with JsonSerializable {
    def id: Id[User]
    def toJson(): JsValue = Json.toJson(this)
  }
  object UserCommand {
    final case class USER_CREATION(id: Id[User] = UserId.generate(), email: String, first_name: String, last_name: String)
      extends UserCommand
    final case class USER_UPDATE(id: Id[User], first_name: Option[String], last_name: Option[String])
      extends UserCommand
  }

  sealed trait UserEvent extends Product with Serializable with JsonSerializable {
    def id: Id[User]
    def toJson(): JsValue = Json.toJson(this)
  }
  object UserEvent {
    final case class USER_CREATED(id: Id[User], email: String, first_name: String, last_name: String)  extends UserEvent
    final case class USER_UPDATED(id: Id[User], first_name: Option[String], last_name: Option[String]) extends UserEvent
  }
  final case class UserState(user: Option[User])

  def apply(): EventSourcedBehavior[UserCommand, UserEvent, UserState] = new EventSourcedBehavior(
    "USER",
    UserState(None),
    commandHandler,
    eventHandler
  )

  private[this] def commandHandler(state: UserState, cmd: UserCommand): Fail \/ UserEvent = {
    import UserCommand._

    (state.user, cmd) match {
      case (Some(_), cmd: USER_CREATION)                         => -\/(Fail(s"A user already exist for email ${cmd.email}"))
      case (None, cmd: USER_CREATION)                            => \/-(UserEvent.USER_CREATED(UserId.generate(), cmd.email, cmd.first_name, cmd.last_name))
      case (None, USER_UPDATE(id, _, _))                         => -\/(Fail(s"No user exist for id $id"))
      case (Some(user), cmd: USER_UPDATE) if (user.id != cmd.id) => -\/(Fail(s"Forbidden to update user with id ${user.id}"))
      case (Some(user), cmd: USER_UPDATE)                        => \/-(UserEvent.USER_UPDATED(user.id, cmd.first_name, cmd.last_name))
    }
  }

  private[this] def eventHandler(state: UserState, event: UserEvent): UserState = {
    (state.user, event) match {
      case (None, evt: UserEvent.USER_CREATED)       => UserState(
          Some(User(
            id         = evt.id,
            email      = evt.email,
            first_name = evt.first_name,
            last_name  = evt.last_name
          ))
        )
      case (Some(_), _: UserEvent.USER_CREATED)      => state // should not happend
      case (None, _)                                 => state // should not happend
      case (Some(user), evt: UserEvent.USER_UPDATED) => state.copy(user =
          Some(
            user.copy(
              first_name = evt.first_name.getOrElse(user.first_name),
              last_name  = evt.last_name.getOrElse(user.last_name)
            )
          )
        )
      case _                                         => state // should not happend
    }
  }

}
