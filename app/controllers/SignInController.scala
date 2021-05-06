package controllers

import models.daos.UserDAO
import play.api.mvc._
import services.PasswordHasherService

import javax.inject.{ Inject, Singleton }
import scala.concurrent.ExecutionContext

/**
 * Controller for handling user sign-ups
 */
@Singleton
class SignInController @Inject() (
  val cc: MessagesControllerComponents,
  userDao: UserDAO,
  passwordHasherService: PasswordHasherService)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  val postUrl: Call = routes.SignInController.handleSignIn

  def handleSignIn: Action[AnyContent] = Action {
    Ok("Signed In")
  }

  def showSignInPage: Action[AnyContent] = Action {
    Ok("Not the sign in page")
  }

}
