package models.daos

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ ExecutionContext, Future }
import slick.jdbc.{ JdbcProfile, PostgresProfile }
import models.Album
import models.tables.AlbumTable

@Singleton
class AlbumDaoImpl @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends AlbumDao
  with AlbumTable {

  protected val driver: JdbcProfile = PostgresProfile
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // Brings DB operations into scope
  import dbConfig._
  import profile.api._

  // Saves the given album into the db
  override def save(album: Album): Future[Album] = db.run {
    albums returning albums += album
  }
}
