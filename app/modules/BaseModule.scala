package modules

import com.google.inject.AbstractModule
import models.daos.{AlbumDAO, AlbumDAOImpl, ImageDAO, ImageDAOImpl, UserDAO, UserDAOImpl}
import services.{
  AWSCloudStorageImpl,
  CloudStorageService,
  PasswordHasherService,
  PasswordHasherServiceImpl
}
import net.codingwell.scalaguice.ScalaModule

/**
 * The base Guice module.
 */
class BaseModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  override def configure(): Unit = {
    bind[UserDAO].to[UserDAOImpl]
    bind[AlbumDAO].to[AlbumDAOImpl]
    bind[ImageDAO].to[ImageDAOImpl]
    bind[PasswordHasherService].to[PasswordHasherServiceImpl]
    bind[CloudStorageService].to[AWSCloudStorageImpl]
  }
}
