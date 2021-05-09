package controllers

import controllers.forms.SignInForm._
import models.User
import models.daos.UserDAO
import play.api.data.Form
import play.api.mvc._
import services.PasswordHasherService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/**
 * Controller for handling user sign-ins
 */
@Singleton
class SignInController @Inject() (
    val cc: MessagesControllerComponents,
    userDao: UserDAO,
    passwordHasherService: PasswordHasherService
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(cc) {

  // URL for the POST request for sign-ins
  val postUrl: Call = routes.SignInController.handleSignIn

  /**
   * Handles GET requests to the Sign In page
   *
   * @return `Action` to display Sign In Page
   */
  def showSignInPage: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.signin(signInForm, postUrl))
  }

  /**
   * Handles Sign In requests
   *
   * @return `Action` to handle Sign In form submission
   */
  def handleSignIn: Action[AnyContent] = Action.async {
    implicit request: MessagesRequest[AnyContent] =>
      // Redirect back to Sign In page
      val errorFunction = { badForm: Form[UserSignInData] =>
        Future.successful(BadRequest(views.html.signin(badForm, postUrl)))
      }

      val successFunction: UserSignInData => Future[Result] = {
        case UserSignInData(signInUsername, signInPassword) =>
          userDao.find(signInUsername).flatMap {
            // username + password match
            case Some(User(_, username, _, password))
                if passwordHasherService.check(signInPassword, password) =>
              Future.successful(
                Redirect(routes.UsersController.showUser(username))
                  .withSession("username" -> username)
              )
            case _ =>
              // username + password don't match
              val formWithInvalidCredentials =
                signInForm.withGlobalError("Invalid username and/or password.")
              errorFunction(formWithInvalidCredentials)
          }
      }

      signInForm.bindFromRequest().fold(errorFunction, successFunction)
  }

}
