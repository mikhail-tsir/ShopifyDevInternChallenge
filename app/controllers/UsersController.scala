package controllers

import controllers.actions._
import controllers.forms.AddAlbumForm.{AddAlbumData, addAlbumForm}
import controllers.forms.SearchUserForm
import controllers.forms.UploadImageForm.{UploadImageData, uploadImageForm}
import models.{Album, Image, User}
import models.daos._

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.Files
import play.api.libs.Files.TemporaryFile.temporaryFileToFile
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import services.CloudStorageService

import java.io.File
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

/**
 * Controller for displaying User pages
 */
@Singleton
class UsersController @Inject() (
    val cc: ControllerComponents,
    cloudStorageService: CloudStorageService,
    authenticatedUserAction: AuthenticatedUserAction
)(implicit
    ec: ExecutionContext,
    userDao: UserDAO,
    albumDao: AlbumDAO,
    imageDao: ImageDAO
) extends AbstractController(cc)
    with I18nSupport {

  val authenticatedAlbumViewerAction = (albumId: Int) =>
    authenticatedUserAction
      .andThen(AlbumAction(albumId))
      .andThen(AlbumViewerAction.apply)

  val authenticatedAlbumOwnerAction = (albumId: Int) =>
    authenticatedUserAction
      .andThen(AlbumAction(albumId))
      .andThen(AlbumOwnerAction.apply)

  /**
   * Helper functions and declarations
   */

  implicit val searchUserForm: Form[String] = SearchUserForm.searchUserForm

  val addAlbumUrl: Call = routes.UsersController.handleAddAlbum

  def uploadImageUrl(album: Album): Call =
    routes.UsersController.handleUploadImageRoute(s"${album.id.getOrElse(-1)}/upload")

  def userNotFoundPage(implicit request: UserRequest[AnyContent]): Future[Result] = {
    Future.successful(NotFound(views.html.userNotFound()))
  }

  def albumNotFoundPage(implicit request: UserRequest[AnyContent]): Future[Result] = {
    Future.successful(NotFound(views.html.albumNotFound()))
  }

  def renderHomePage(implicit request: UserRequest[AnyContent]): Future[Result] = {
    for (_ <- userDao.getAlbums(request.user)) yield {
      Redirect(routes.UsersController.showUser(request.user.username), SEE_OTHER)
    }
  }

  def invalidRoute = Action { _ =>
    BadRequest("Invalid route.")
  }

  def invalidRouteAuth =
    authenticatedUserAction(parse.multipartFormData).async { _ =>
      Future.successful {
        Redirect(routes.UsersController.invalidRoute)
      }
    }

  /**
   * Application route actions
   */

  def showUser(username: String): Action[AnyContent] = authenticatedUserAction.async {
    implicit request: UserRequest[AnyContent] =>
      userDao.find(username).flatMap {
        // If the searched-for user exists
        case Some(user) =>
          val albums =
            if (user == request.user) userDao.getAlbums(user)
            else userDao.getPublicAlbums(user)

          // Get albums
          albums.flatMap { albumList =>
            Future.successful(Ok(views.html.userAlbums(user, albumList)))
          }
        // If the searched-for user does not exist
        case None => userNotFoundPage
      }
  }

  def search(username: String): Action[AnyContent] = showUser(username)

  def viewAlbum(id: Int): Action[AnyContent] = authenticatedAlbumViewerAction(id).async {
    implicit request: AlbumRequest[AnyContent] =>
      implicit val userRequest: UserRequest[AnyContent] = request.request
      (
        for {
          owner  <- userDao.getAlbumOwner(request.album)
          images <- imageDao.getImages(request.album) if owner.isDefined
        } yield {
          val imgList: List[(Image, String)] = images.map { img =>
            (img, cloudStorageService.getBase64Encoding(img.location))
          }
          owner match {
            case Some(o) =>
              Ok(
                views.html.userTemplate(
                  views.html.albumContents(request.album, o, imgList)
                )
              )
            case None => NotFound(views.html.albumNotFound())
          }
        }
      ).recover { case e: Exception =>
        Ok(views.html.albumError())
      }
  }

  def showAddAlbumPage: Action[AnyContent] = authenticatedUserAction {
    implicit request: UserRequest[AnyContent] =>
      Ok(views.html.addAlbum(addAlbumForm, addAlbumUrl))
  }

  def handleAddAlbum: Action[AnyContent] = authenticatedUserAction.async {
    implicit request: UserRequest[AnyContent] =>
      val errorFunction = { badForm: Form[AddAlbumData] =>
        Future.successful(BadRequest(views.html.addAlbum(badForm, addAlbumUrl)))
      }

      val successFunction: AddAlbumData => Future[Result] = {
        case AddAlbumData(name, description, public) =>
          albumDao
            .save(Album(None, request.user.id, name, description, public))
            .flatMap { _ => renderHomePage }
      }

      addAlbumForm.bindFromRequest().fold(errorFunction, successFunction)
  }

  def deleteAlbum(id: Int): Action[AnyContent] = authenticatedAlbumOwnerAction(id).async {
    implicit request: AlbumRequest[AnyContent] =>
      implicit val userRequest: UserRequest[AnyContent] = request.request
      albumDao.delete(id).flatMap { _ =>
        renderHomePage
      }
  }

  def validateFileType(filename: String): Boolean = {
    val allowedExtensions: Set[String] = Set("jpg", "png")
    allowedExtensions.contains(filename.split("\\.").toList.last)
  }

  /**
   * Validates the route suffix for uploading image
   *
   * @param suffix The route suffix
   * @return The id of the object or None if suffix is invalid
   */
  def validateUploadImageRoute(suffix: String): Option[Int] = {
    val components = suffix.split("/").toList
    val isValid: Boolean = components.length == 2 &&
      components.head.toIntOption.isDefined && (
        components(1) == "upload" ||
          components(1) == "upload/"
      )

    if (isValid) components.head.toIntOption else None
  }

  def showUploadImagePageRoute(suffix: String) =
    validateUploadImageRoute(suffix) match {
      case Some(id) => showUploadImagePage(id)
      case _        => invalidRoute
    }

  def showUploadImagePage(albumId: Int) = authenticatedAlbumOwnerAction(albumId) {
    implicit request: AlbumRequest[AnyContent] =>
      Ok(views.html.uploadImage(postUrl = uploadImageUrl(request.album)))
  }

  def handleUploadImageRoute(suffix: String) = {
    validateUploadImageRoute(suffix) match {
      case Some(id) => handleUploadImage(id)
      case _        => invalidRouteAuth
    }
  }

  def handleUploadImage(albumId: Int) = {
    authenticatedAlbumOwnerAction(albumId)(parse.multipartFormData).async { implicit request =>
      val album = request.album
      val errorFunction: String => Form[UploadImageData] => Future[Result] =
        (flashMessage: String) =>
          _ =>
            Future.successful(
              Redirect(routes.UsersController.showUploadImagePageRoute(s"$albumId/upload"))
                .flashing("error" -> flashMessage)
            )

      val successFunction: (File, String) => UploadImageData => Future[Result] =
        (image, extension) => {
          uploadImageData: UploadImageData =>
            val filename   = UUID.randomUUID().toString + "." + extension
            val imageModel = Image(None, uploadImageData.caption, album.id, filename)
            (
              for {
                _ <- cloudStorageService.uploadImage(image, filename)
                _ <- imageDao.save(imageModel)
              } yield {
                Redirect(
                  routes.UsersController.viewAlbum(album.id.getOrElse(-1))
                ).flashing("success" -> "Image uploaded successfully!")
              }
            ).recover { case _: Exception =>
              Redirect(
                routes.UsersController.viewAlbum(album.id.getOrElse(-1))
              ).flashing("error" -> "There was an error uploading your image.")
            }
        }

      request.body
        .file("File")
        .map { image =>
          if (validateFileType(image.filename)) {
            val extension = image.filename.split("\\.").last
            uploadImageForm
              .bindFromRequest()
              .fold(
                errorFunction("Error uploading image."),
                successFunction(temporaryFileToFile(image.ref), extension)
              )
          } else
            errorFunction("Invalid file type (must be .jpg or .png).")(uploadImageForm)
        }
        .getOrElse(
          errorFunction("Missing image.")(uploadImageForm)
        )
    }
  }

  def deleteImageRoute(suffix: String) = {
    println("DELETING IMAGE")
    val components = suffix.split("/").toList
    val isValid: Boolean = components.length == 3 &&
      components.head.toIntOption.isDefined &&
      components(1).toIntOption.isDefined && (
        components(2) == "delete" ||
          components(2) == "delete/"
      )

    if (isValid) (components.head.toIntOption, components(1).toIntOption) match {
      case (Some(albumId), Some(imageId)) => handleDeleteImage(albumId, imageId)
      case _ => {
        println("INVALID ROUTE 1")
        invalidRoute
      }
    }
    else {
      println("INVALID ROUTE 2")
      invalidRoute
    }
  }

  def handleDeleteImage(albumId: Int, imageId: Int) =
    authenticatedAlbumOwnerAction(albumId).async { _ =>
      imageDao.delete(imageId).map { _ =>
        Redirect(routes.UsersController.viewAlbum(albumId))
      }
    }

  // Log out action
  def logout: Action[AnyContent] = Action {
    Redirect(routes.SignInController.showSignInPage).withNewSession
  }

}
