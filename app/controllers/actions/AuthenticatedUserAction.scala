package controllers.actions

import controllers.routes
import models.User
import models.daos.UserDAO
import play.api.i18n.MessagesApi
import play.api.mvc.Results._
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/**
 * A wrapped request containing the original request and the
 *  currently logged-in user
 */
case class UserRequest[A] @Inject() (user: User, request: Request[A])(implicit
    messagesApi: MessagesApi
) extends WrappedRequest[A](request)

    /**
     * An action that handles incoming `UserRequest`s and blocks
     * the request if the user isn't logged in.
     *
     * Adds the current `User` object to the request so that it
     *  can be accessed by other Actions requiring the user to be logged in.
     */
class AuthenticatedUserAction @Inject() (val parser: BodyParsers.Default)(implicit
    val executionContext: ExecutionContext,
    userDao: UserDAO,
    messagesApi: MessagesApi
) extends ActionBuilder[UserRequest, AnyContent] {
  override def invokeBlock[A](
      request: Request[A],
      block: UserRequest[A] => Future[Result]
  ): Future[Result] = {

    // If not logged in, redirects to Sign In page
    val notAuthenticatedAction = Future.successful(
      Redirect(routes.SignInController.showSignInPage).withNewSession
        .flashing("info" -> "Please log in to access that page")
    )

    val authenticatedAction = (username: String) =>
      userDao.find(username).flatMap {
        // If logged in and the current user exists, proceeds with the request and
        //  adds headers to clear cache so that the page isn't cached after user logs out
        case Some(user) =>
          block(UserRequest(user, request)).map {
            _.withHeaders(
              "Cache-Control" -> "no-cache, no-store, must-revalidate",
              "Pragma"        -> "no-cache",
              "Expires"       -> "0"
            )
          }
        // If the current user doesn't exist
        case None => notAuthenticatedAction
      }

    request.session
      .get("username")
      .fold(notAuthenticatedAction)(authenticatedAction)
  }
}
