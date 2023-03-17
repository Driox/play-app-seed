package services

import domain._

import akka.actor.ActorSystem
import event.infrastructure._
import event.infrastructure.pulsar.EventSourcingClient
import models.JsonParser
import play.api.libs.json.Reads

import javax.inject._

@Singleton
class UserEventSourced @Inject() (
  val event_sourcing_client: EventSourcingClient
  // val event_sourcing_client: DBEventSourcingClient
)(implicit val system:       ActorSystem)
  extends EntityEventSourced[
    User,
    UserPersistentBehavior.UserCommand,
    UserPersistentBehavior.UserEvent,
    UserPersistentBehavior.UserState
  ] with JsonParser {
  protected val entity_json_parser: Reads[UserPersistentBehavior.UserEvent] = userEventJsonParser

  protected val behaviour = UserPersistentBehavior.apply()
}

// check this https://doc.akka.io/docs/akka/current/typed/persistence.html
// @Singleton
// class UserEventSourced2 @Inject() (event_sourcing_client: EventSourcingClient)(implicit system: ActorSystem) extends Sorus with DisjunctionHelper with JsonParser {

//   private[this] val behaviour = UserPersistentBehavior.apply()

//   private[this] val empty_state: Fail \/ PersistedState[UserPersistentBehavior.UserState] =
//     \/-(PersistedState(behaviour.emptyState, 0))

//   def commandHandler(
//     created_by: String,
//     cmd:        UserPersistentBehavior.UserCommand
//   ): Step[Event[UserPersistentBehavior.UserEvent]] = {
//     val p_state_f = cmd match {
//       case _: USER_CREATION => Future.successful(empty_state)
//       case x                => replayEvent(x.id, behaviour.persistenceType)
//     }
//     for {
//       p_state   <- p_state_f                                    ?| ()
//       payload   <- behaviour.commandHandler(p_state.state, cmd) ?| ()
//       event_name = EventName(payload.getClass().getSimpleName())
//       event      = Event(
//                      name        = event_name,
//                      sequence_nb = p_state.last_sequence_nb + 1L,
//                      created_by  = created_by,
//                      entity_id   = payload.id,
//                      entity_type = behaviour.persistenceType,
//                      payload     = payload
//                    )
//       _         <- event_sourcing_client.persist(event)         ?| ()
//     } yield {
//       event
//     }
//   }

//   def replayEvent(
//     entity_id:   Id[User],
//     entity_type: String
//   ): Future[Fail \/ PersistedState[UserPersistentBehavior.UserState]] = {
//     // TODO event : handle stream supervision & co
//     event_sourcing_client
//       .reload_event[User, UserPersistentBehavior.UserEvent](entity_type, entity_id)(userEventJsonParser)
//       .runWith(
//         Sink.fold(empty_state)((state, event) => {
//           for {
//             s <- state
//             e <- event
//           } yield {
//             val new_state = behaviour.eventHandler(s.state, e.payload)
//             PersistedState(new_state, e.sequence_nb)
//           }
//         })
//       )
//   }
// }
