package controllers

import domain._

import services._

import akka.actor.ActorSystem
import event._
import event.infrastructure.pulsar.EventSourcingClient
import helpers.sorus.Fail
import models.JsonParser
import play.api.libs.json._
import play.api.mvc._
import scalaz.\/
import tagged.Tags._

import javax.inject._
import scala.concurrent.ExecutionContext

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class EventSourceControllerTest @Inject() (
  event_sourcing_client: EventSourcingClient,
  user_client:           UserEventSourced,
  parse:                 PlayBodyParsers
)(implicit ec:           ExecutionContext, system: ActorSystem) extends controllers.BaseController with JsonParser {

  def all(entity_id: String, entity_type: String) = Action.async { _ =>
    val empty: List[Fail \/ Event[_]] = List()
    event_sourcing_client.reload_event[User, UserPersistentBehavior.UserEvent](
      entity_type,
      entity_id.asInstanceOf[Id[User]]
    )
      .runFold(empty) { (acc, elem) =>
        acc :+ elem
      }
      .map(x => x.mkString("\n"))
      .map(Ok(_))
  }

  def last_entity(entity_id: String, entity_type: String) = Action.async { implicit request =>
    val entity_id_validated = entity_id.asInstanceOf[Id[User]]
    for {
      result <- user_client.replayEvent(entity_id_validated, entity_type) ?| ()
    } yield {
      Ok(Json.toJson(result.state))
    }
  }

  def create(entity_type: String) = Action.async(parse.json) { implicit request =>
    // TODO : load it from credentials
    val created_by = "context-id-1234"

    for {
      cmd    <- request.body.validate[UserPersistentBehavior.UserCommand.USER_CREATION] ?| ()
      result <- user_client.commandHandler(created_by, cmd)
    } yield {
      Ok(result.toJson())
    }
  }

  def update(entity_id: String, entity_type: String) = Action.async(parse.json) { implicit request =>
    // TODO : load it from credentials
    val created_by = "context-id-1234"

    for {
      json_body <- request.body.asOpt[JsObject]                                      ?| "Body should be a json object"
      json_obj   = json_body ++ Json.obj("id" -> entity_id)
      cmd       <- json_obj.validate[UserPersistentBehavior.UserCommand.USER_UPDATE] ?| ()
      result    <- user_client.commandHandler(created_by, cmd)
    } yield {
      Ok(result.toJson())
    }
  }

}
