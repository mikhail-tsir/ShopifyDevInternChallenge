package models
import play.api.libs.json.{ Json, OFormat }

case class Album(
  id: Option[Int],
  user_id: Option[Int],
  name: String,
  description: String,
  isPublic: Boolean)

object Album {
  implicit val albumFormat: OFormat[Album] = Json.format[Album]
}
