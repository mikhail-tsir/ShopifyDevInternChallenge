package models.tables

import models.Album
import slick.jdbc.JdbcProfile

private[models] trait AlbumTable extends UserTable {
  protected val driver: JdbcProfile
  import driver.api._

  class Albums(tag: Tag) extends Table[Album](tag, Some("image_repo"), "albums") {

    /** ID column, auto-incremented primary key */
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    /** Id of owner of Album */
    def user_id = column[Int]("user_id")

    /** Name column */
    def name = column[String]("name")

    /** Description column */
    def description = column[String]("description")

    /** Public/private flag column */
    def is_public = column[Boolean]("is_public")

    /** Mapping between table row and Album case class */
    def * = (id.?, user_id.?, name, description, is_public) <> ((Album.apply _).tupled, Album.unapply)

    /** Foreign key on Users table */
    def owner = foreignKey("owner_fk", user_id, users)(_.id)
  }

}
