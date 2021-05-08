package models

import play.api.libs.json.{ Json, OFormat }

case class Image(id: Option[Int], caption: String, albumId: Option[Int], location: String)

object Image {
  implicit val imageFormat: OFormat[Image] = Json.format[Image]
}
