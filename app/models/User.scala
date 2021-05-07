package models

import play.api.libs.json._

import scala.concurrent.Future

/**
 * User model
 *
 * @param id Unique id of user
 * @param username User's (unique) username
 * @param name User's full name
 * @param password User's password
 */
case class User(id: Option[Int], username: String, name: String, password: String) {
  /**
   * Check if user is authorized to access the given album
   *
   * @param album The album to check authorization against
   * @return true if authorized, false otherwise
   */
  def hasAccessTo(album: Album): Boolean = album.isPublic || album.user_id == id

}

object User {
  implicit val userFormat: OFormat[User] = Json.format[User]
}
