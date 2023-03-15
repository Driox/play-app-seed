import pl.iterators.kebs.tagged._
import utils.{ StringUtils, TimeUtils }

import java.time.{ OffsetDateTime, ZonedDateTime }

package object event {
  trait EventIdTag
  type EventId = String @@ EventIdTag

  object EventId {
    def apply(arg: String): String @@ EventIdTag = arg.taggedWith[EventIdTag]
    def generate(): String @@ EventIdTag         = EventId("evt_" + StringUtils.generateUuid())
  }

  trait EventNameTag
  type EventName = String @@ EventNameTag

  object EventName {
    val EMPTY: String @@ EventNameTag                                      = EventName("EMPTY")
    def apply(arg: String): String @@ EventNameTag = arg.taggedWith[EventNameTag]
  }

  trait TimestampTag
  type Timestamp = Long @@ TimestampTag

  object Timestamp {
    def apply(arg: Long): Long @@ TimestampTag           = arg.taggedWith[TimestampTag]
    def generate(): Long @@ TimestampTag                 = Timestamp(TimeUtils.now.toInstant.toEpochMilli)
    def from(date: ZonedDateTime): Long @@ TimestampTag  = Timestamp(date.toInstant.toEpochMilli)
    def from(date: OffsetDateTime): Long @@ TimestampTag = Timestamp(date.toInstant.toEpochMilli)
  }
}
