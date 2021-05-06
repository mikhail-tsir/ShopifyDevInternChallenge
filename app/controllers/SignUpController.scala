package controllers

import controllers.forms.SignUpForm._
import models.User

import javax.inject._
import models.daos.UserDAO
import play.api.data.Form
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import services.PasswordHasherService

/**
 * Controller for handling user sign-ups
 */
@Singleton
class SignUpController @Inject() (
    val cc: MessagesControllerComponents,
    userDao: UserDAO,
    passwordHasherService: PasswordHasherService
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(cc) {

  val postUrl: Call = routes.SignUpController.handleSignUp

  /** Loads sign up page */
  def showSignUpPage: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.signup(signUpForm, postUrl))
  }

  /**
   * Processes user sign up
   * TODO implement this
   */
  def handleSignUp: Action[AnyContent] = Action.async {
    implicit request: MessagesRequest[AnyContent] =>
      val errorFunction = { badForm: Form[UserSignUpData] =>
        Future.successful(BadRequest(views.html.signup(badForm, postUrl)))
      }

      val successFunction: UserSignUpData => Future[Result] = {
        case UserSignUpData(username, name, password, _) =>
          userDao.find(username).flatMap {
            // Username taken
            case Some(_) =>
              val formWithBadUsername =
                signUpForm.withError(key = "Username", message = "Username already taken")
              errorFunction(formWithBadUsername)
            // Username not taken
            case None =>
              val hashedPassword = passwordHasherService.hash(password)
              userDao
                .save(User(None, username, name, hashedPassword))
                .map(_ =>
                  Redirect(routes.SignInController.handleSignIn)
                    .flashing("success" -> s"Welcome, $username!")
                )
          }
      }

      signUpForm.bindFromRequest().fold(errorFunction, successFunction)
  }
}
