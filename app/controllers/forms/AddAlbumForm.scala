package controllers.forms

import play.api.data.Form
import play.api.data.Forms._

object AddAlbumForm {
  case class AddAlbumData(name: String, Description: String, public: Boolean)

  /** Add album form */
  val addAlbumForm: Form[AddAlbumData] = Form(
    mapping(
      "Name of Album" -> nonEmptyText(minLength = 3),
      "Description" -> text(maxLength = 150),
      "public" -> boolean)(AddAlbumData.apply)(AddAlbumData.unapply))
}
