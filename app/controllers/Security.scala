package controllers

import models._
import models.dao.DaoAware
import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

/**
 * Created by Grignou on 30/10/2015.
 */
class AuthRequest[A](val user: User, request: Request[A]) extends WrappedRequest[A](request)

private[controllers] trait AuthController extends Controller with DaoAware {

  object AuthAction extends ActionBuilder[AuthRequest] with ActionRefiner[Request, AuthRequest] {
    def refine[A](request: Request[A]): Future[Either[Result, AuthRequest[A]]] = reqToUser(request) map {
      case Some(user) => Right(new AuthRequest(user, request))
      case None       => Left(Forbidden("error.login.required"))
    }
  }

  private def reqToUser(request: RequestHeader): Future[Option[User]] = {
    request.session.get("userEmail").map { email =>
      userDao.findByEmail(email)
    }.getOrElse(Future.successful(None))
  }
}
