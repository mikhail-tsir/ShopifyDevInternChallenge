package controllers.forms

import play.api.data.Form
import play.api.data.Forms._

object SignUpForm {

  case class UserSignUpData(
    username: String,
    name: String,
    password: String,
    confirmPassword: String)

  /** Sign Up form */
  val signUpForm: Form[UserSignUpData] = Form(
    mapping(
      "Username" -> nonEmptyText,
      "Name" -> nonEmptyText,
      "Password" -> nonEmptyText(minLength = 8),
      "Confirm Password" -> nonEmptyText(minLength = 8))(UserSignUpData.apply)(UserSignUpData.unapply)
      .verifying(
        "Passwords don't match",
        { case UserSignUpData(_, _, password, confirmPassword) => password == confirmPassword }))

}
