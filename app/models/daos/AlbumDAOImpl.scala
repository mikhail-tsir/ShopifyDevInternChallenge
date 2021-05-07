package models.daos

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ ExecutionContext, Future }
import slick.jdbc.{ JdbcProfile, PostgresProfile }
import models.{ Album, User }
import models.tables.AlbumTable

/**
 * Data Accessor Object for Albums, handles all DB interaction
 */
@Singleton
class AlbumDAOImpl @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends AlbumDAO
  with AlbumTable {

  protected val driver: JdbcProfile = PostgresProfile
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // Brings DB operations into scope
  import dbConfig._
  import profile.api._

  // Finds an album given its id
  override def find(id: Int): Future[Option[Album]] = db.run {
    albums.filter(_.id === id).result.headOption
  }

  // Saves the given album into the db
  override def save(album: Album): Future[Album] = db.run {
    albums returning albums += album
  }

}
