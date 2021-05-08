package controllers

import controllers.forms.AddAlbumForm.{AddAlbumData, addAlbumForm}
import controllers.forms.SearchUserForm
import models.{Album, User}
import models.daos.{AlbumDAO, UserDAO}

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
 * Controller for displaying User pages
 */
@Singleton
class UsersController @Inject() (
    val cc: ControllerComponents,
    userDao: UserDAO,
    albumDao: AlbumDAO,
    authenticatedUserAction: AuthenticatedUserAction
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with I18nSupport {

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

  // This needs to be in a separate function because the showUser route
  //  is /user/<username>, but the searchUserForm cannot call that action.
  //  Instead it calls this route which is /search?username=<username>.
  def search(username: String): Action[AnyContent] = showUser(username)

  def viewAlbum(id: Int): Action[AnyContent] = authenticatedUserAction.async {
    implicit request: UserRequest[AnyContent] =>
      albumDao.find(id).flatMap {
        // Album exists and current user is authorized to view it
        case Some(album) =>
          if (request.user.hasAccessTo(album))
            userDao.getAlbumOwner(album).flatMap {
              // If the album owner exists
              case Some(owner) =>
                Future
                  .successful(Ok(views.html.userTemplate(views.html.albumContents(album, owner))))
              // If the Album owner doesn't exist
              case None => albumNotFoundPage
            }
          // User is not authorized to view album
          else userNotFoundPage
        // Album does not exist
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

  def deleteAlbum(id: Int): Action[AnyContent] = authenticatedUserAction.async {
    implicit request: UserRequest[AnyContent] =>
      albumDao.find(id).flatMap {
        case Some(album) if album.user_id == request.user.id =>
          albumDao.delete(album).flatMap { _ =>
            renderHomePage
          }
        case _ =>
          Future.successful(Unauthorized("You do not have permission to delete this album."))
      }
  }

  // Log out action
  def logout: Action[AnyContent] = Action {
    Redirect(routes.SignInController.showSignInPage).withNewSession
  }

}
