package event

import tagged.Tags.Id
import helpers.sorus.Fail
import scalaz.\/

trait CommandType[T] {
  def id: Id[T]
}

trait EventType[T] {
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

// TODO event : a voir si on en a encore besoin, optimistic lockin ?
// we encapsulate persistance into this class
case class PersistedState[S](state: S, last_sequence_nb: Long)
