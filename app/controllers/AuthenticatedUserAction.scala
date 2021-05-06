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
    request.session
      .get("username")
      .fold(
        Future.successful(
          Redirect(routes.SignInController.showSignInPage)
            .flashing("info" -> "Please log in to access that page")))(username => block(UserRequest(username, request)))
  }
}
