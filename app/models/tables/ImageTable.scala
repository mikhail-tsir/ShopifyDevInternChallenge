package models.tables

import models.Image
import slick.jdbc.JdbcProfile

private[models] trait ImageTable extends AlbumTable {
  protected val driver: JdbcProfile
  import driver.api._

  /**
   * Table definition for the `image_repo.images` table
   */
  class Images(tag: Tag) extends Table[Image](tag, Some("image_repo"), "images") {

    /** ID column, auto-incremented primary key */
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    /** Caption column */
    def caption = column[String]("caption")

    /** ID of album to which image belongs */
    def album_id = column[Int]("album_id")

    /** Location of image in cloud storage */
    def location = column[String]("location")

    def * = (id.?, caption, album_id.?, location) <> ((Image.apply _).tupled, Image.unapply)

    def album_fk = foreignKey("album_fk", album_id, albums)(_.id)
  }

  lazy val images = TableQuery[Images]
}
