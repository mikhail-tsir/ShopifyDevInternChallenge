package models.daos

import models.Album
import scala.concurrent.Future

trait AlbumDao {
  def find(username: String): Future[List[Album]]

  def findByName(name: String): Future[Option[Album]]

  def save(album: Album)
}
