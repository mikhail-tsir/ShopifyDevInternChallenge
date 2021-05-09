package models

import play.api.libs.json._

import scala.concurrent.Future

/**
 * Model for describing and working with Users
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
   * @param album The album to check view permission against
   * @return true if authorized, false otherwise
   */
  def canView(album: Album): Boolean = album.isPublic || (id.isDefined && id == album.user_id)

  /**
   * Check if user is authorized to delete given album (or an image in it)
   *
   * @param album The album to check delete permission against
   * @return true if authorized, false otherwise
   */
  def canDelete(album: Album): Boolean = id.isDefined && id == album.user_id
}

object User {
  implicit val userFormat: OFormat[User] = Json.format[User]
}
