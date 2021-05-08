package controllers.forms

import models.Image

import java.io.File
import play.api.data.Form
import play.api.data.Forms._

object UploadImageForm {
  case class UploadImageData(image: Option[File], caption: String)

  val uploadImageForm: Form[UploadImageData] = Form(
    mapping(
      "File"    -> ignored(Option.empty[File]),
      "Caption" -> nonEmptyText(maxLength = 200)
    )(UploadImageData.apply)(UploadImageData.unapply)
  )
}
