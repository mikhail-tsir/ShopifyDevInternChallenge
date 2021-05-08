package services

import models.Image

import java.io.File
import scala.concurrent.Future

trait CloudStorageService {

  /**
   * Uploads image to cloud storage
   *
   * @param file Image to upload
   * @return Id of uploaded image
   */
  def uploadImage(file: File): Future[Int]
}
