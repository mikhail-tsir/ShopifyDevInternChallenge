package models.daos

import models.Album
import scala.concurrent.Future

trait AlbumDao {

  /**
   * Saves the given album into the db
   *
   * @param album The album to save
   * @return The saved album
   */
  def save(album: Album): Future[Album]
}
