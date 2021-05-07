package controllers

import models.User
import models.daos.UserDAO

import javax.inject.{ Inject, Singleton }
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Controller for displaying User pages
 */
@Singleton
class UsersController @Inject() (
  val cc: ControllerComponents,
  userDao: UserDAO,
  authenticatedUserAction: AuthenticatedUserAction,
  messagesApi: MessagesApi)(implicit ec: ExecutionContext)
  extends AbstractController(cc)
  with I18nSupport {
  val searchUserForm: Form[String] = Form(nonEmptyText)

  def showUser(username: String): Action[AnyContent] = authenticatedUserAction.async {
    implicit request: UserRequest[AnyContent] =>
      userDao.find(request.username).flatMap {
        // If the current user exists
        case Some(curUser) =>
          userDao.find(username).flatMap {
            userOpt: Option[User] =>
              Future.successful(
                Ok(views.html.users(curUser, userOpt, searchUserForm)))
          }
        case _ => Future.successful(NotFound(views.html.notfound()))
      }
  }

  def search(username: String): Action[AnyContent] = showUser(username)

  def logout: Action[AnyContent] = Action {
    Redirect(routes.SignInController.showSignInPage).withNewSession
  }

}
