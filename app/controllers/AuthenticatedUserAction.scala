package controllers

import models.User
import models.daos.UserDAO

import javax.inject.Inject
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.{ ExecutionContext, Future }

case class UserRequest[A](user: User, request: Request[A]) extends WrappedRequest[A](request)

class AuthenticatedUserAction @Inject() (val parser: BodyParsers.Default)(implicit
  val executionContext: ExecutionContext,
  userDao: UserDAO) extends ActionBuilder[UserRequest, AnyContent] {
  override def invokeBlock[A](
    request: Request[A],
    block: UserRequest[A] => Future[Result]): Future[Result] = {

    // If not logged in, redirects to Sign In page
    val notAuthenticatedAction = Future.successful(
      Redirect(routes.SignInController.showSignInPage)
        .withNewSession
        .flashing("info" -> "Please log in to access that page"))

    val authenticatedAction = (username: String) =>
      userDao.find(username).flatMap {
        // If logged in and the current user exists, proceeds with the request and
        //  adds headers to clear cache so that the page isn't cached after user logs out
        case Some(user) => block(UserRequest(user, request)).map {
          _.withHeaders(
            "Cache-Control" -> "no-cache, no-store, must-revalidate",
            "Pragma" -> "no-cache",
            "Expires" -> "0")
        }
        // If the current user doesn't exist
        case None => notAuthenticatedAction
      }

    request.session
      .get("username")
      .fold(notAuthenticatedAction)(authenticatedAction)
  }
}
