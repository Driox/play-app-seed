package event.infrastructure

import tagged.Tags.Id
import event._
import helpers.sorus.SorusDSL._
import helpers.DisjunctionHelper
import scala.concurrent.Future
import scalaz.{ \/, \/- }
import helpers.sorus.Fail
import akka.stream.scaladsl.Sink
import play.api.libs.json._
import akka.actor.ActorSystem

trait EntityEventSourced[
  EntityType,
  Cmd <: CommandType[EntityType],
  Evt <: EventType[EntityType],
  State
] extends Sorus with DisjunctionHelper {

  /**
    * These fields should be injected
    *
    */
  implicit def system: ActorSystem
  def event_sourcing_client: EventSourcing

  /**
  * Define it or extends JsonParser
  */
  protected def entity_json_parser: Reads[Evt]

  /**
    * Should come from domain
    */
  protected def behaviour: EventSourcedBehavior[EntityType, Cmd, Evt, State]
  protected def is_creation_command(cmd: Cmd): Boolean

  private[this] lazy val empty_state: Fail \/ PersistedState[State] = \/-(PersistedState(behaviour.emptyState, 0))

  def commandHandler(
    created_by: String,
    cmd:        Cmd
  ): Step[Event[Evt]] = {
    val p_state_f = if(false && is_creation_command(cmd)) {
      Future.successful(empty_state)
    } else {
      replayEvent(cmd.id, behaviour.persistenceType)
    }
    for {
      p_state   <- p_state_f                                    ?| ()
      payload   <- behaviour.commandHandler(p_state.state, cmd) ?| ()
      event_name = EventName(payload.getClass().getSimpleName())
      event      = Event(
                     name        = event_name,
                     sequence_nb = p_state.last_sequence_nb + 1L,
                     created_by  = created_by,
                     entity_id   = payload.id,
                     entity_type = behaviour.persistenceType,
                     payload     = payload
                   )
      _         <- event_sourcing_client.persist(event)         ?| ()
    } yield {
      event
    }
  }

  def replayEvent(
    entity_id:   Id[EntityType],
    entity_type: String
  ): Future[Fail \/ PersistedState[State]] = {
    // TODO : handle stream supervision & co
    event_sourcing_client
      .reload_event[EntityType, Evt](entity_type, entity_id)(entity_json_parser)
      .runWith(
        Sink.fold(empty_state)((state, event) => {
          for {
            s <- state
            e <- event
          } yield {
            val new_state = behaviour.eventHandler(s.state, e.payload)
            PersistedState(new_state, e.sequence_nb)
          }
        })
      )
  }
}
