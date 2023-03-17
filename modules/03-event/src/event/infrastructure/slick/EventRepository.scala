package event.infrastructure.slick

import event._
import helpers.sorus.Fail
import helpers.sorus.SorusDSL.Sorus
import models.dao._
import play.api.db.slick._
import play.api.libs.json.JsObject
import scalaz.\/
import slick.basic.DatabasePublisher
import slick.jdbc.{ ResultSetConcurrency, ResultSetType }
import tagged.Tags.Id

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class EventRepository @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec:                    ExecutionContext)
  extends HasDatabaseConfigProvider[EnhancedPostgresDriver]
    with EventDao
    with Sorus
    with SlickUtils {

  import profile.api._

  val tables = TableQuery[EventTable]
  type TableType = EventTable

  def persist(event: Event[JsObject]): Future[Fail \/ Int] = option2fail {
    val query = tables ++= Seq(event)
    db.run(query)
  }("error.event.on_persist")

  def search[EntityType](
    entity_id:   Id[EntityType],
    entity_type: String,
    criteria:    EventSearchCriteria
  ): DatabasePublisher[Event[JsObject]] = {
    val query =
      tables
        .filter(_.entity_type === entity_type)
        .filter(_.entity_id === entity_id)
        .filterOpt(criteria.event_name)(_.name === _)
        .filterOpt(criteria.created_by)(_.created_by === _)
        .filterOpt(criteria.publish_after)(_.created_at > _)
        .filterOpt(criteria.sequence_nb_after)(_.sequence_nb > _)

    db.stream(
      query
        .result
        .withStatementParameters(
          rsType        = ResultSetType.ForwardOnly,
          rsConcurrency = ResultSetConcurrency.ReadOnly,
          fetchSize     = 10000
        )
        .transactionally
    )
  }
}
