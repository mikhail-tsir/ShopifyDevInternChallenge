package controllers

import controllers.actions.{
  AlbumAction,
  AlbumOwnerAction,
  AlbumRequest,
  AlbumViewerAction,
  AuthenticatedUserAction,
  UserRequest
}
import controllers.forms.AddAlbumForm.{AddAlbumData, addAlbumForm}
import controllers.forms.SearchUserForm
import controllers.forms.UploadImageForm.{UploadImageData, uploadImageForm}
import models.{Album, Image, User}
import models.daos.{AlbumDAO, UserDAO}

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.Files
import play.api.mvc._
import services.CloudStorageService

import java.io.File
import java.nio.file.Paths
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
    albumDao: AlbumDAO
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
      userDao.getAlbumOwner(request.album).flatMap {
        case Some(owner) =>
          Future.successful {
            Ok(
              views.html.userTemplate(
                views.html.albumContents(request.album, owner)
              )
            )
          }
        // If we can't retrieve the page of the album's owner
        case None => albumNotFoundPage
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
      albumDao.delete(request.album).flatMap { _ =>
        renderHomePage
      }
  }

  //  def validateFileType(filename: String): Boolean = {
  //    val allowedExtensions: Set[String] = Set("jpg", "png")
  //    allowedExtensions.contains(filename.split(".").last)
  //  }
  //
  //  def uploadImage(album: Album): Action[MultipartFormData[_]] = {
  //    authenticatedUserAction(parse.multipartFormData).async {
  //      implicit request: UserRequest[MultipartFormData[Files.TemporaryFile]] =>
  //        val errorFunction = (flashMessage: String) => {
  //          _: Form[UploadImageData] =>
  //            Future.successful(
  //              Redirect(routes.UsersController.showUploadImagePage(album))
  //                .flashing("error" -> flashMessage)
  //            )
  //        }
  //
  //        val successFunction = (image: File) => {
  //          uploadImageData: UploadImageData =>
  //            val imageModel = Image(None, uploadImageData.caption, album.id, image.getName)
  //            for {
  //              id <- cloudStorageService.uploadImage(image)
  //              _  <- imageDao
  //            } yield Redirect(
  //              routes.UsersController.viewAlbum(album.id.getOrElse(-1))
  //            )
  //              .flashing("success" -> "Image uploaded successfully!")
  //        }
  //
  //        request.body
  //          .file("File")
  //          .map { image =>
  //            if (validateFileType(image.filename)) {}
  //            val uuid      = UUID.randomUUID()
  //            val extension = image.filename.split(".").last
  //            val tempFile  = new File(s"tmp/$uuid.$extension")
  //            image.ref.moveTo(tempFile)
  //
  //            uploadImageForm.bindFromRequest
  //              .fold(errorFunction("Error uploading image."), successFunction(tempFile))
  //          }
  //          .getOrElse(
  //            errorFunction("Missing image.")
  //          )
  //    }
  //  }

  // Log out action
  def logout: Action[AnyContent] = Action {
    Redirect(routes.SignInController.showSignInPage).withNewSession
  }

}
