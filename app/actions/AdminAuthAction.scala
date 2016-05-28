/*
 * Copyright (c) 2016 morpheby
 */

package actions

import javax.inject._

import models.User
import play.api.mvc._
import services.CredentialsService

import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedRequest[A] (val user: User, request: Request[A]) extends WrappedRequest[A](request)

/**
  * Created by morpheby on 27.5.16.
  */
//class AdminAuth[A] (action: Action[A], credService: CredentialsService)(implicit exec: ExecutionContext) extends Action[A] {
//  def apply(request: Request[A]): Future[Result] = {
//    (request.session.get(credService.checkAuthCookieKey).map { authKey =>
//      credService.checkAuth(authKey)
//    } getOrElse { Future.successful(None) }).flatMap(_.map { user =>
//      action(new AuthenticatedRequest[A](user, request))
//    } getOrElse {
//      Future.successful(Results.Forbidden("forbidden"))
//    })
//  }
//
//  lazy val parser = action.parser
//}


@Singleton
class AuthAction @Inject() (credService: CredentialsService)(implicit exec: ExecutionContext) extends ActionBuilder[AuthenticatedRequest] {
  def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]) = {
    (request.session.get(credService.checkAuthCookieKey).map { authKey =>
      credService.checkAuth(authKey)
    } getOrElse { Future.successful(None) }).flatMap(_.map { user =>
      block(new AuthenticatedRequest[A](user, request))
    } getOrElse {
      Future.successful(Results.Forbidden("forbidden"))
    })

  }
//  override def composeAction[A](action: Action[A]) = new AdminAuth(action, credService)
}
