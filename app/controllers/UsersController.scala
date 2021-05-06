package controllers

import models.daos.UserDAO

import javax.inject.{ Inject, Singleton }
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Controller for displaying User pages
 */
@Singleton
class UsersController @Inject() (
  val cc: ControllerComponents,
  userDao: UserDAO,
  authenticatedUserAction: AuthenticatedUserAction)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def showUser(username: String): Action[AnyContent] = authenticatedUserAction.async {
    implicit request: UserRequest[AnyContent] =>
      userDao.find(username).flatMap {
        case Some(user) => Future.successful(Ok(views.html.users(user)))
        case None => Future.successful(Results.NotFound)
      }
  }

  def logout: Action[AnyContent] = Action {
    Redirect(routes.SignInController.showSignInPage).withNewSession
  }

}
