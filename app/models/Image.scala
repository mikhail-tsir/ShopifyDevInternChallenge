package models

import play.api.libs.json.{Json, OFormat}

/**
 * Model for describing and working with Images
 * @param id id of image
 * @param caption caption of image
 * @param albumId id of album to which the image belongs
 * @param location file name of image in cloud storage
 */
case class Image(id: Option[Int], caption: String, albumId: Option[Int], location: String)

object Image {
  implicit val imageFormat: OFormat[Image] = Json.format[Image]
}
