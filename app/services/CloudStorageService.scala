package services

import models.Image

import java.io.File
import scala.concurrent.Future

/**
 * Abstract service for connecting to the cloud
 */
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
   * Deletes the given image from cloud storage
   *
   * @param filename The name of the file to delete
   */
  def deleteImage(filename: String): Future[Unit]

  /**
   * Gets the base64 encoding of an image
   *
   * @param filename The name of the image to encode
   * @return The Base64 encoding of the image
   */
  def getBase64Encoding(filename: String): String
}
