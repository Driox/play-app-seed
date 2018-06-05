package controllers

import models._
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by Grignou on 30/10/2015.
 */
class AuthRequest[A](val user: User, request: Request[A]) extends WrappedRequest[A](request)

private[controllers] trait Security { self: BaseController =>

  def userDao: Users

  def secureAction()(implicit ec: ExecutionContext) = new ActionRefiner[Request, AuthRequest] {
    def executionContext = ec
    def refine[A](request: Request[A]): Future[Either[Result, AuthRequest[A]]] = reqToUser(request) map {
      case Some(user) => Right(new AuthRequest(user, request))
      case None       => Left(Forbidden("error.login.required"))
    }
  }

  def AuthAction()(implicit ec: ExecutionContext): ActionBuilder[AuthRequest, AnyContent] = Action andThen secureAction

  private def reqToUser(request: RequestHeader)(implicit ec: ExecutionContext): Future[Option[User]] = {
    request.session.get("userEmail").map { email =>
      userDao.findByEmail(email)
    }.getOrElse(Future.successful(None))
  }
}
