package models
import play.api.libs.json.{ Json, OFormat }

case class Album(
  id: Option[Int],
  user_id: Option[Int],
  name: String,
  description: String,
  isPublic: Boolean) {

  /**
   * Returns the description up to a given number of words
   *  and appends `...` if there is leftover text.
   * @param numWords the max number of words to take from description
   * @return The sliced description
   */
  def capDescription(numWords: Int = 30): String = {
    val words = description.split(" ")
    if (words.length <= numWords) description
    else words.take(numWords) :+ "..." mkString " "
  }
}

object Album {
  implicit val albumFormat: OFormat[Album] = Json.format[Album]
}
