/*
 * Copyright (c) 2016 morpheby
 */

package controllers

import akka.actor.ActorSystem
import javax.inject._

import actions.AuthAction
import play.api._
import play.api.mvc._
import services._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration._
import play.api.libs.functional._
import play.api.libs.json._


/**
  * Created by morpheby on 24.5.16.
  */
@Singleton
class ClientRegController @Inject()(actorSystem: ActorSystem,
                                    authAction: AuthAction, credentialService: CredentialsService)
                                   (implicit exec: ExecutionContext) extends Controller {

  case class AuthData(login: String, password: Option[String], otp: Option[Int])

  implicit val authDataFormat = Json.reads[AuthData]

  case class InvalidAuthException() extends Exception("Invalid authentication JSON")

  def index = authAction {
    Ok("Pass")
  }

  def authFilterRecover(block: (Throwable) => Result): PartialFunction[Throwable, Result] = {
    {
      case e@(InvalidAuthException() | AuthenticationException() |
              InvalidOperationException(_) | InvalidInputException()) => block(e)
      case e@(SessionExpired()) => block(e).withNewSession
    }
  }

  def adminAuth() = Action.async(parse.json) { r =>

    val authDataJs = r.body.validate[AuthData]

    authDataJs.map {
      case AuthData(login, Some(password), Some(otp)) =>
        credentialService.authAdmin(login, password, otp).map { authKey =>
          Ok("gotcha").withSession(credentialService.checkAuthCookieKey -> authKey)
        }
    } getOrElse {
      Future.failed(InvalidAuthException())
    } recover authFilterRecover { e => Ok(e.getMessage) }
  }

  def registerNew() = Action.async(parse.json) { r =>

    val authDataJs = r.body.validate[AuthData]

    (authDataJs.map {
      case AuthData(login, maybePass, None) =>
        credentialService.createAdmin(login, maybePass,
          r.session.get(credentialService.checkAuthCookieKey)).map { u =>
          Ok(u.name).withNewSession
        }
    } getOrElse {
      Future.failed(InvalidAuthException())
    }) recover authFilterRecover { e => Ok(e.getMessage) }
  }

  def unregister(login: String) = authAction.async { r =>
    credentialService.deleteAdmin(login).map(_ => Ok("deleted")) recover authFilterRecover { e => Ok(e.getMessage) }
  }

  def finishReg() = Action.async(parse.json) { r =>

    val authDataJs = r.body.validate[AuthData]

    (authDataJs.map {
      case AuthData(login, Some(password), None) =>
        credentialService.finishRegistration(login, password).map { u =>
          Ok(u).withNewSession
        }
    } getOrElse {
      Future.failed(InvalidAuthException())
    }) recover authFilterRecover { e => Ok(e.getMessage) }
  }
}
