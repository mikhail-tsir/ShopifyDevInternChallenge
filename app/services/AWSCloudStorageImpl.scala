package services

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import services.CloudStorageService
import software.amazon.awssdk.core.waiters.WaiterResponse
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.waiters.S3Waiter
import software.amazon.awssdk.services.s3.model._

import java.io.File
import java.nio.file.Path
import scala.concurrent.{ExecutionContext, Future}

/**
 * Implementation of the CloudStorageService trait providing
 *  an interface for AWS s3
 */
@Singleton
class AWSCloudStorageImpl @Inject() (implicit config: Configuration, ec: ExecutionContext)
    extends CloudStorageService {
  val region: Region     = Region.of(config.get[String]("aws.s3.region"))
  val bucketName: String = config.get[String]("aws.s3.bucketname")

  val s3: S3Client = S3Client
    .builder()
    .region(region)
    .build()

  override def uploadImage(file: File): Future[Int] = {
    val key: String = file.getName
    val objectRequest: PutObjectRequest = PutObjectRequest
      .builder()
      .bucket(bucketName)
      .key(key)
      .build()

    //s3.putObject(objectRequest, RequestBody.fromFile())
    Future { 0 }

  }

}
