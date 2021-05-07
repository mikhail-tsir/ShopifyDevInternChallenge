package models.daos

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ ExecutionContext, Future }
import slick.jdbc.{ JdbcProfile, PostgresProfile }
import models.{ Album, User }
import models.tables.{ AlbumTable, UserTable }

/**
 * Data accessor object for Users
 */
@Singleton
class UserDAOImpl @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends UserDAO
  with UserTable
  with AlbumTable {

  protected val driver: JdbcProfile = PostgresProfile
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // Brings DB operations into scope
  import dbConfig._
  import profile.api._

  override def find(username: String): Future[Option[User]] = db.run {
    users.filter(_.username === username).result.headOption
  }

  override def save(user: User): Future[User] = db.run {
    users returning users += user
  }

  override def update(user: User): Future[User] = db.run {
    users.filter(_.username === user.username).update(user).map(_ => user)
  }

  override def getAlbums(user: User): Future[List[Album]] = db.run {
    albums.filter(_.user_id === user.id).result
  }.map(_.toList)

  override def getPublicAlbums(user: User): Future[List[Album]] = db.run {
    albums.filter(album => album.user_id === user.id && album.is_public).result
  }.map(_.toList)

}
