package controllers.forms

import play.api.data.Form
import play.api.data.Forms._

object searchUserForm {
  val searchUserForm: Form[String] = Form(nonEmptyText)
}
