package controllers

import javax.inject.Inject
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.{ ExecutionContext, Future }

case class UserRequest[A](username: String, request: Request[A]) extends WrappedRequest[A](request)

class AuthenticatedUserAction @Inject() (val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext) extends ActionBuilder[UserRequest, AnyContent] {
  override def invokeBlock[A](
    request: Request[A],
    block: UserRequest[A] => Future[Result]): Future[Result] = {

    // If not logged in, redirects to Sign In page
    val notAuthenticatedAction = Future.successful(
      Redirect(routes.SignInController.showSignInPage)
        .flashing("info" -> "Please log in to access that page"))

    // If logged in, proceeds with the request and adds headers to clear cache
    //  so that the page isn't cached after user logs out
    val authenticatedAction = (username: String) =>
      block(UserRequest(username, request)).map { result =>
        result.withHeaders(
          "Cache-Control" -> "no-cache, no-store, must-revalidate",
          "Pragma" -> "no-cache",
          "Expires" -> "0")
      }

    request.session
      .get("username")
      .fold(notAuthenticatedAction)(authenticatedAction)
  }
}
