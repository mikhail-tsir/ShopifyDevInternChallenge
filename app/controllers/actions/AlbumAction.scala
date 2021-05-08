package controllers.actions

import models.daos.AlbumDAO
import models.{Album, User}
import play.api.i18n.MessagesApi
import play.api.mvc.Results.NotFound
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/**
 * A wrapped request containing the original UserRequest and an Album,
 *  used inside actions that operate on an Album instance
 */
case class AlbumRequest[A] @Inject() (album: Album, request: UserRequest[A])(implicit
    messagesApi: MessagesApi
) extends WrappedRequest[A](request) {
  def user: User = request.user
}

/**
 * Action for handling albums.
 * Can be chained with `AlbumPermissionAction`s to block
 *  unauthorized operations.
 */
object AlbumAction {
  @Inject()
  def apply(albumId: Int)(implicit
      ec: ExecutionContext,
      albumDao: AlbumDAO,
      messagesApi: MessagesApi
  ) =
    new ActionRefiner[UserRequest, AlbumRequest] {
      def executionContext: ExecutionContext = ec
      def refine[A](userRequest: UserRequest[A]) =
        albumDao
          .find(albumId)
          .map { albumOpt =>
            albumOpt
              .map(AlbumRequest(_, userRequest))
              .toRight(NotFound(views.html.albumNotFound()(userRequest)))
          }
    }
}

/**
 * Interface for creating actions that handle album-related permissions
 *  (e.g. if a user can view/delete the album)
 *  Note that permissions for any given album are the same for the
 *  images in it
 */
trait AlbumPermissionAction {

  /**
   * Abstract check for if the albumRequest should be authorized
   *
   * @param albumRequest The incoming request containing a User and an Album
   * @tparam A Content type of request
   * @return True if authorized, false otherwise
   */
  def check[A](albumRequest: AlbumRequest[A]): Boolean

  /**
   * Constructs a filter that applies a permissions check to the
   *  incoming request and block it if unauthorized
   */
  def apply(implicit ec: ExecutionContext) =
    new ActionFilter[AlbumRequest] {
      def executionContext: ExecutionContext = ec
      def filter[A](albumRequest: AlbumRequest[A]) =
        Future.successful {
          if (check(albumRequest)) None
          else
            Some(
              NotFound(views.html.albumNotFound()(albumRequest.request))
            )
        }
    }
}

/**
 * Permission action for viewing albums
 */
object AlbumViewerAction extends AlbumPermissionAction {
  def check[A](albumRequest: AlbumRequest[A]): Boolean =
    albumRequest.user.canView(albumRequest.album)
}

/**
 * Permission action for deleting albums
 */
object AlbumOwnerAction extends AlbumPermissionAction {
  def check[A](albumRequest: AlbumRequest[A]): Boolean =
    albumRequest.user.canDelete(albumRequest.album)
}
