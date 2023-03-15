package event

import helpers.sorus.Fail
import scalaz.\/

case class EventSourcedBehavior[Command, EventPayload, State](
  persistenceType: String,
  emptyState:      State,
  commandHandler:  (State, Command) => Fail \/ EventPayload,
  eventHandler:    (State, EventPayload) => State
)

// TODO event : a voir si on en a encore besoin, optimistic lockin ?
// we encapsulate persistance into this class
case class PersistedState[S](state: S, last_sequence_nb: Long)
