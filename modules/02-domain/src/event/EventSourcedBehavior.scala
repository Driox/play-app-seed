package event

import tagged.Tags.Id
import helpers.sorus.Fail
import scalaz.\/
import utils.json.JsonSerializable

trait CommandType[T] {
  def id: Id[T]
}

trait EventType[T] extends JsonSerializable {
  def id: Id[T]
}

case class EventSourcedBehavior[
  EntityType,
  Cmd <: CommandType[EntityType],
  Evt <: EventType[EntityType],
  State
](
  persistenceType: String,
  emptyState:      State,
  commandHandler:  (State, Cmd) => Fail \/ Evt,
  eventHandler:    (State, Evt) => State
)

// TODO event decuplication : a voir si on en a encore besoin, optimistic lockin ? related to the TODO in PulsarPublisher.build_message
// we encapsulate persistance into this class
case class PersistedState[S](state: S, last_sequence_nb: Long)
