package models.tables

import models.User
import slick.jdbc.JdbcProfile

private[models] trait UserTable {
  protected val driver: JdbcProfile
  import driver.api._

  class Users(tag: Tag) extends Table[User](tag, Some("image_repo"), "userAlbums") {

    /** ID column, auto-incremented primary key */
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    /** Name column */
    def name = column[String]("name")

    /** Username column, unique identifier */
    def username = column[String]("username", O.Unique)

    /** Password column (hashed) */
    def password = column[String]("password")

    def * = (id.?, username, name, password) <> ((User.apply _).tupled, User.unapply)
  }

  lazy val users = TableQuery[Users]

}
