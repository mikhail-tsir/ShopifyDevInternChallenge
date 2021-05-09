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
 * Controller for handling everything a User does
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

  /**
   * Factories for creating actions containing the given album
   */

  // Current user has "viewer" permissions
  val authenticatedAlbumViewerAction = (albumId: Int) =>
    authenticatedUserAction
      .andThen(AlbumAction(albumId))
      .andThen(AlbumViewerAction.apply)

  // Current user has "owner" permissions
  val authenticatedAlbumOwnerAction = (albumId: Int) =>
    authenticatedUserAction
      .andThen(AlbumAction(albumId))
      .andThen(AlbumOwnerAction.apply)

  /**
   * Helper functions and declarations
   */

  // Implicit declaration of the `Search Username` form so we don't have
  //  instantiate it for every user page.
  implicit val searchUserForm: Form[String] = SearchUserForm.searchUserForm

  val addAlbumUrl: Call = routes.UsersController.handleAddAlbum

  /**
   * Generates the URL for POST requests for uploading images to the given album
   * @param album The album to upload to
   * @return `Call` to be used in the Upload Image form submission
   */
  def uploadImageUrl(album: Album): Call =
    routes.UsersController.handleUploadImageRoute(s"${album.id.getOrElse(-1)}/upload")

  // Fallback for asynchronous actions to render the `User Not Found` page
  def userNotFoundPage(implicit request: UserRequest[AnyContent]): Future[Result] = {
    Future.successful(NotFound(views.html.userNotFound()))
  }

  // Fallback for asynchronous actions to render the `Album Not Found` page
  def albumNotFoundPage(implicit request: UserRequest[AnyContent]): Future[Result] = {
    Future.successful(NotFound(views.html.albumNotFound()))
  }

  // Renders the user page for the current user
  def renderHomePage(implicit request: UserRequest[AnyContent]): Future[Result] = {
    for (_ <- userDao.getAlbums(request.user)) yield {
      Redirect(routes.UsersController.showUser(request.user.username), SEE_OTHER)
    }
  }

  // Fallback for actions with dynamic routes where the route is invalid
  def invalidRoute = Action { _ =>
    BadRequest("Invalid route.")
  }

  // Same as above but for actions that operate on `MultipartFormData`
  //  (otherwise there's a type mismatch)
  def invalidRouteAuth =
    authenticatedUserAction(parse.multipartFormData).async { _ =>
      Future.successful {
        Redirect(routes.UsersController.invalidRoute)
      }
    }

  /**
   * Application route actions
   */

  /**
   * Renders a user's home page with all their albums
   */
  def showUser(username: String) = authenticatedUserAction.async {
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

  /**
   * Handles user search requests (calls the `showUser` action on the username)
   * @param username The username to search
   */
  def search(username: String): Action[AnyContent] = showUser(username)

  /**
   * Renders the page for a user's Album
   *
   * @param id The id of the album to render
   */
  def viewAlbum(id: Int) = authenticatedAlbumViewerAction(id).async {
    implicit request: AlbumRequest[AnyContent] =>
      // implicit UserRequest to be picked up by the HTML template
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
      ).recover {
        // Could not get album owner or could now load images from cloud storage
        case _: Exception => Ok(views.html.albumError())
      }
  }

  /**
   * Renders the `Add Album` page for the current user
   */
  def showAddAlbumPage: Action[AnyContent] = authenticatedUserAction {
    implicit request: UserRequest[AnyContent] =>
      Ok(views.html.addAlbum(addAlbumForm, addAlbumUrl))
  }

  /**
   * Handles `Add Album` form submission POST requests
   */
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

  /**
   * Handles DELETE requests to delete the given album
   *
   * @param id id of album to delete
   */
  def deleteAlbum(id: Int): Action[AnyContent] = authenticatedAlbumOwnerAction(id).async {
    implicit request: AlbumRequest[AnyContent] =>
      implicit val userRequest: UserRequest[AnyContent] = request.request
      albumDao.delete(id).flatMap { _ =>
        renderHomePage
      }
  }

  /**
   * Validates file extensions for image upload
   *
   * @param filename The name of the file to check
   * @return True if extension is allowed, false otherwise.
   */
  def validateFileType(filename: String): Boolean = {
    val allowedExtensions: Set[String] = Set("jpg", "png", "jpeg")
    allowedExtensions.contains(filename.split("\\.").toList.last)
  }

  /**
   * Validates the route suffix for uploading images.
   * Must be of the form <album_id>/upload
   *
   * @param suffix The route suffix (after /album/)
   * @return The id of the album or None if suffix is invalid
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

  /**
   * Validates and routes GET requests to upload image page
   *
   * @param suffix The suffix of the route (after /album/)
   * @return The action to render the `Upload Image` page or
   *         error page if suffix is invalid
   */
  def showUploadImagePageRoute(suffix: String) =
    validateUploadImageRoute(suffix) match {
      case Some(id) => showUploadImagePage(id)
      case _        => invalidRoute
    }

  /**
   * Displays the `Upload Image` form for the given album
   *
   * @param albumId The id of the album to upload to
   */
  def showUploadImagePage(albumId: Int) = authenticatedAlbumOwnerAction(albumId) {
    implicit request: AlbumRequest[AnyContent] =>
      Ok(views.html.uploadImage(postUrl = uploadImageUrl(request.album)))
  }

  /**
   * Validates and routes POST requests for upload image form submission
   *
   * @param suffix The suffix of the route (after /album/)
   * @return The action to submit the form or error page if suffix is invalid
   */
  def handleUploadImageRoute(suffix: String) = {
    validateUploadImageRoute(suffix) match {
      case Some(id) => handleUploadImage(id)
      case _        => invalidRouteAuth
    }
  }

  /**
   * Handles image uploads
   *
   * @param albumId The id of the album to upload to
   */
  def handleUploadImage(albumId: Int) = {
    authenticatedAlbumOwnerAction(albumId)(parse.multipartFormData).async { implicit request =>
      val album = request.album
      // Generates an error function to display the given message
      val errorFunction: String => Form[UploadImageData] => Future[Result] =
        (flashMessage: String) =>
          _ =>
            Future.successful(
              Redirect(routes.UsersController.showUploadImagePageRoute(s"$albumId/upload"))
                .flashing("error" -> flashMessage)
            )

      // Generates a success function to upload the given file with the given file type
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
            ).recover { case e: Exception =>
              println("EXCEPTION: ")
              println(e)
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

  /**
   * Validates and routes DELETE requests for deleting images.
   * Route suffix ix valid if it is of the form `<album_id>/<image_id>/delete`
   *
   * @param suffix Suffix of the route (after /album/)
   * @return The action to delete the image if suffix is valid
   *         or error page otherwise
   */
  def deleteImageRoute(suffix: String) = {
    val components = suffix.split("/").toList
    val isValid: Boolean = components.length == 3 &&
      components.head.toIntOption.isDefined &&
      components(1).toIntOption.isDefined && (
        components(2) == "delete" ||
          components(2) == "delete/"
      )

    if (isValid) (components.head.toIntOption, components(1).toIntOption) match {
      case (Some(albumId), Some(imageId)) => handleDeleteImage(albumId, imageId)
      case _                              => invalidRoute
    }
    else invalidRoute
  }

  /**
   * Handles deleting images
   *
   * @param albumId The album to delete from
   * @param imageId The image to delete
   */
  def handleDeleteImage(albumId: Int, imageId: Int) =
    authenticatedAlbumOwnerAction(albumId).async { _ =>
      imageDao.delete(imageId).map { _ =>
        Redirect(routes.UsersController.viewAlbum(albumId))
          .flashing("success" -> "Image deleted successfully")
      }
    }

  /**
   * Handles POST requests for logging out
   */
  def logout: Action[AnyContent] = Action {
    Redirect(routes.SignInController.showSignInPage).withNewSession
  }

}
