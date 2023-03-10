package event

sealed trait EventName extends Product with Serializable

object EventName {

  final case object USER_CREATED extends EventName
  final case object USER_UPDATED extends EventName

  val values: Set[EventName] = Set(USER_CREATED, USER_UPDATED)

  def parse(s: String): Option[EventName] = values.find(_.productPrefix == s.toUpperCase)
}
