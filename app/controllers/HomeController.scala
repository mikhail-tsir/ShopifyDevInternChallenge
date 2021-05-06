package controllers

import javax.inject._
import play.api._
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the application's home page.
 */
@Singleton
class HomeController @Inject() (val controllerComponents: ControllerComponents)
  extends BaseController {

  /**
   * Redirects to Sign Up page
   */
  def index(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Redirect(routes.SignUpController.showSignUpPage)
  }
}
