package models.daos

import models.User

import scala.concurrent.Future

trait UserDAO {

  /**
   * Finds a user by their login info
   *
   * @param username User's user name (unique)
   * @return The found user or None if not found
   */
  def find(username: String): Future[Option[User]]

  /**
   * Saves the given user
   *
   * @param user The user to save
   * @return The saved user
   */
  def save(user: User): Future[User]

  /**
   * Updates an existing user
   *
   * @param user The user to update
   * @return The newly updated user
   */
  def update(user: User): Future[User]
}
