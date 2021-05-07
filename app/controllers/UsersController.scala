package controllers

import models.{ Album, User }
import models.daos.{ AlbumDAO, UserDAO }

import javax.inject.{ Inject, Singleton }
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.mvc._
import play.twirl.api.Html

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Controller for displaying User pages
 */
@Singleton
class UsersController @Inject() (
  val cc: ControllerComponents,
  userDao: UserDAO,
  albumDao: AlbumDAO,
  authenticatedUserAction: AuthenticatedUserAction)(implicit ec: ExecutionContext)
  extends AbstractController(cc)
  with I18nSupport {

  implicit val searchUserForm: Form[String] = Form(nonEmptyText)

  def userNotFoundPage(implicit request: UserRequest[AnyContent]): Future[Result] = {
    Future.successful(NotFound(views.html.userNotFound()))
  }

  def albumNotFoundPage(implicit request: UserRequest[AnyContent]): Future[Result] = {
    Future.successful(NotFound(views.html.albumNotFound()))
  }

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
                Future.successful(
                  Ok(views.html.userTemplate(views.html.albumContents(album, owner))))
              // If the Album owner doesn't exist
              case None => albumNotFoundPage
            }
          // User is not authorized to view album
          else userNotFoundPage
        // Album does not exist
        case None => albumNotFoundPage
      }
  }

  // Log out action
  def logout: Action[AnyContent] = Action {
    Redirect(routes.SignInController.showSignInPage).withNewSession
  }

}
