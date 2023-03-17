package event.infrastructure.slick

import event._
import models.dao.{ EnhancedPostgresDriver, SlickUtils }
import play.api.db.slick.HasDatabaseConfig
import play.api.libs.json._

trait EventDao extends DomainEventMapping with SlickUtils {
  self: HasDatabaseConfig[EnhancedPostgresDriver] =>

  import profile.api._

  class EventTable(tag: Tag) extends Table[Event[JsObject]](tag, "events") {

    val id          = column[EventId]("id", O.PrimaryKey)
    val name        = column[EventName]("name")
    val sequence_nb = column[Long]("sequence_nb")
    val created_at  = column[Timestamp]("created_at")
    val created_by  = column[String]("created_by")
    val entity_id   = column[String]("entity_id")
    val entity_type = column[String]("entity_type")
    val payload     = column[JsObject]("payload")
    val tags        = column[Set[String]]("tags")

    def * =
      (
        id,
        name,
        sequence_nb,
        created_at,
        created_by,
        entity_id,
        entity_type,
        payload,
        tags
      ) <> ((mapperTo _).tupled, unapply _)

    private[this] def mapperTo(
      id:          EventId,
      name:        EventName,
      sequence_nb: Long,
      created_at:  Timestamp,
      created_by:  String,
      entity_id:   String,
      entity_type: String,
      payload:     JsObject,
      tags:        Set[String]
    ) = Event(
      id,
      name,
      sequence_nb,
      created_at,
      created_by,
      entity_id,
      entity_type,
      payload,
      tags
    )

    private[this] def unapply(event: Event[JsObject]) =
      Option((
        event.id,
        event.name,
        event.sequence_nb,
        event.created_at,
        event.created_by,
        event.entity_id,
        event.entity_type,
        event.payload,
        event.tags
      ))
  }
}
