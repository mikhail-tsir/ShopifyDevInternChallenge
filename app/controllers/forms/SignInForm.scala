package controllers.forms

import play.api.data.Form
import play.api.data.Forms._

object SignInForm {
  case class UserSignInData(username: String, password: String)

  val signInForm: Form[UserSignInData] = Form(
    mapping("Username" -> nonEmptyText, "Password" -> nonEmptyText)(UserSignInData.apply)(
      UserSignInData.unapply
    )
  )
}
