/*
 * Copyright (c) 2016 morpheby
 */

package services

import javax.inject._

import com.warrenstrange.googleauth.{GoogleAuthenticator, GoogleAuthenticatorKey, GoogleAuthenticatorQRGenerator}
import models.User
import org.mindrot.jbcrypt.BCrypt
import play.api.cache.{CacheApi, NamedCache}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Random
import scala.concurrent.duration._
import services.AdminAuthExceptions._

/**
  * Created by morpheby on 27.5.16.
  */

trait AdminCredentialsService {
  def createAdmin(name: String, password: Option[String], authKey: Option[String]): Future[User]

  def authAdmin(name: String, password: String, otp: Int) : Future[String]

  def checkAuth(authKey: String): Future[Option[User]]

  def finishRegistration(name: String, password: String): Future[String]

  def deleteAdmin(name: String): Future[Unit]

  val checkAuthCookieKey: String
}

object AdminAuthExceptions {

  case class AdminAuthenticationException() extends Exception("Authentication failed")

  case class InvalidOperationException(message: String) extends Exception(s"Invalid operation was attempted: ${message}")

  case class InvalidInputException() extends Exception("Input data is invalid")

  case class SessionExpired() extends Exception("Session expired")

}

@Singleton
class DummyAdminCredentialService @Inject()(@NamedCache("session-cache") sessionCache: CacheApi)
                                           (implicit exec: ExecutionContext) extends AdminCredentialsService {

  val gAuth = new GoogleAuthenticator()
  val otpKey: GoogleAuthenticatorKey = gAuth.createCredentials()
  var dummy = new User("dummy", Some(BCrypt.hashpw("12345678", BCrypt.gensalt(12))), Some(otpKey.getKey))

  val checkAuthCookieKey = "dummyCredToken"

  def createAdmin(name: String, password: Option[String], authKey: Option[String]): Future[User] = {
    Future {
      dummy
    }
  }

  def authAdmin(name: String, password: String, otp: Int) : Future[String] = {
    Future {
      if (BCrypt.checkpw(password, dummy.passwordHash.get) && gAuth.authorize(dummy.otpKey.get, otp)) {
        val sessionKey = Random.nextString(64)
        sessionCache.set(sessionKey, dummy.name, 15.minutes)
        sessionKey
      } else {
        throw AdminAuthenticationException()
      }
    }
  }

  def checkAuth(authKey: String): Future[Option[User]] = {
    Future.successful(
      sessionCache.get[String](authKey).flatMap {name =>
        if(dummy.name == name) {
          sessionCache.set(authKey, name, 15.minutes)
          Some(dummy)
        } else {
          None
        }
      }
    )
  }

  def finishRegistration(name: String, password: String): Future[String] = {
    Future {
      if (BCrypt.checkpw(password, dummy.passwordHash.get)) {
        GoogleAuthenticatorQRGenerator.getOtpAuthURL("DummyAuth", name, otpKey)
      } else {
        throw AdminAuthenticationException()
      }
    }
  }


  def deleteAdmin(name: String): Future[Unit] = {
    Future.successful(Unit)
  }

}
