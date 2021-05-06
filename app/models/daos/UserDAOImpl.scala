package models.daos

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ ExecutionContext, Future }
import slick.jdbc.{ JdbcProfile, PostgresProfile }
import models.User
import models.tables.UserTable

/**
 * Data accessor object for Users
 */
@Singleton
class UserDAOImpl @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends UserDAO
  with UserTable {

  protected val driver: JdbcProfile = PostgresProfile
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // Brings DB operations into scope
  import dbConfig._
  import profile.api._

  private val users = TableQuery[Users]

  override def find(username: String): Future[Option[User]] = db.run {
    users.filter(_.username === username).result.headOption
  }

  override def save(user: User): Future[User] = db.run {
    users returning users += user
  }

  override def update(user: User): Future[User] = db.run {
    users.filter(_.username === user.username).update(user).map(_ => user)
  }

}
