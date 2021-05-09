package services

import models.Image

import java.io.File
import scala.concurrent.Future

trait CloudStorageService {

  /**
   * Uploads image to cloud storage
   *
   * @param file Image to upload
   * @param key Name of file to store in cloud
   * @return Id of uploaded image
   */
  def uploadImage(file: File, key: String): Future[Unit]

  /**
   * Gets the base64 encoding of an image
   *
   * @param filename The name of the image to encode
   * @return The Base64 encoding of the image
   */
  def getBase64Encoding(filename: String): String
}
