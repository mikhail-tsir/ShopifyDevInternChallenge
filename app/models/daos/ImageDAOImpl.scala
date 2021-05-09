package models.daos

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.{JdbcProfile, PostgresProfile}
import models.{Album, Image, User}
import models.tables.ImageTable

/**
 * Data Accessor Object for Albums, handles all DB interaction
 */
@Singleton
class ImageDAOImpl @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit
    ec: ExecutionContext
) extends ImageDAO
    with ImageTable {

  protected val driver: JdbcProfile = PostgresProfile
  private val dbConfig              = dbConfigProvider.get[JdbcProfile]

  // Brings DB operations into scope
  import dbConfig._
  import profile.api._

  // Finds image in db by id
  override def find(id: Int): Future[Option[Image]] = db.run {
    images.filter(_.id === id).result.headOption
  }

  // Saves the image in db
  override def save(image: Image): Future[Image] = db.run {
    images returning images += image
  }

  // Deletes image from db
  override def delete(id: Int): Future[Option[Image]] = {
    val query = images.filter(_.id === id)
    val action = for {
      results <- query.result
      _       <- query.delete
    } yield results.headOption

    db.run(action)
  }

  // Lists location of all images in album
  override def getImages(album: Album): Future[List[Image]] = album.id match {
    case Some(id) =>
      db.run {
        images.filter(_.album_id === id).result
      }.map(_.toList)
    case None => {
      Future.successful(List())
    }
  }

}
