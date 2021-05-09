package models.daos

import models.{Album, Image}

import scala.concurrent.Future

trait ImageDAO {

  /**
   * Finds an image by its id
   *
   * @param id The id of the image to find
   * @return The found image
   */
  def find(id: Int): Future[Option[Image]]

  /**
   * Saves the given image
   * @param image The image to save
   * @return The saved image
   */
  def save(image: Image): Future[Image]

  /**
   * Deletes the given image
   *
   * @param id The id of the image to delete
   * @return The deleted image
   */
  def delete(id: Int): Future[Option[Image]]

  /**
   * Gets the  images in a given album
   *
   * @param album The album to search
   * @return The images in the album
   */
  def getImages(album: Album): Future[List[Image]]
}
