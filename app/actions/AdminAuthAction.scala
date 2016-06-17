/*
 * Copyright (c) 2016 morpheby
 */

package actions

import javax.inject._

import models.User
import play.api.mvc._
import services.AdminCredentialsService

import scala.concurrent.{ExecutionContext, Future}

class AdminAuthenticatedRequest[A](val user: User, request: Request[A]) extends WrappedRequest[A](request)

/**
  * Created by morpheby on 27.5.16.
  */


@Singleton
class AdminAuthAction @Inject()(credService: AdminCredentialsService)(implicit exec: ExecutionContext) extends ActionBuilder[AdminAuthenticatedRequest] {
  def invokeBlock[A](request: Request[A], block: (AdminAuthenticatedRequest[A]) => Future[Result]) = {
    (request.session.get(credService.checkAuthCookieKey).map { authKey =>
      credService.checkAuth(authKey)
    } getOrElse { Future.successful(None) }).flatMap(_.map { user =>
      block(new AdminAuthenticatedRequest[A](user, request))
    } getOrElse {
      Future.successful(Results.Forbidden("forbidden"))
    })

  }
//  override def composeAction[A](action: Action[A]) = new AdminAuth(action, credService)
}
