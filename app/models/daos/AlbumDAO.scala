package models.daos

import models.{ Album, User }
import scala.concurrent.Future

trait AlbumDAO {

  /**
   * Finds album by the given id
   *
   * @param id The id of the album to find
   * @return The found album
   */
  def find(id: Int): Future[Option[Album]]

  /**
   * Saves the given album into the db
   *
   * @param album The album to save
   * @return The saved album
   */
  def save(album: Album): Future[Album]
}
