package models.daos

import models.{ Album, User }

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

  /**
   * Returns a list of all albums belonging to that user
   *
   * @param user The user whose albums are queried
   * @return (Possibly empty) List of albums belonging to that user
   */
  def getAlbums(user: User): Future[List[Album]]

  /**
   * Returns a list of all public albums belonging to a given user
   *
   * @param user The user whose albums are queried
   * @return (Possibly empty) List of public albums belonging to that user
   */
  def getPublicAlbums(user: User): Future[List[Album]]

  /**
   * Returns the owner of the album
   *
   * @param album The album whose owner to retrieve
   * @return The owner of `album`
   */
  def getAlbumOwner(album: Album): Future[Option[User]]
}
