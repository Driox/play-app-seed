import pl.iterators.kebs.tagged._
import utils.{ StringUtils, TimeUtils }
import java.time.{ OffsetDateTime, ZonedDateTime }

package object event {
  trait EventIdTag
  type EventId = String @@ EventIdTag

  object EventId {
    def apply(arg: String) = arg.taggedWith[EventIdTag]
    def generate() = EventId("evt_" + StringUtils.generateUuid())
  }

  trait TimestampTag
  type Timestamp = Long @@ TimestampTag

  object Timestamp {
    def apply(arg: Long) = arg.taggedWith[TimestampTag]
    def generate() = Timestamp(TimeUtils.now.toInstant.toEpochMilli)
    def from(date: ZonedDateTime)  = Timestamp(date.toInstant.toEpochMilli)
    def from(date: OffsetDateTime) = Timestamp(date.toInstant.toEpochMilli)
  }
}
