package models

import play.api.libs.json._

/**
 * User model
 *
 * @param id Unique id of user
 * @param username User's (unique) username
 * @param name User's full name
 * @param password User's password
 */
case class User(id: Option[Int], username: String, name: String, password: String)

object User {
  implicit val userFormat: OFormat[User] = Json.format[User]
}
