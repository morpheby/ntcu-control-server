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

/**
  * Created by morpheby on 27.5.16.
  */

trait CredentialsService {
  def createAdmin(name: String, password: Option[String], authKey: Option[String]): Future[User]

  def authAdmin(name: String, password: String, otp: Int) : Future[String]

  def checkAuth(authKey: String): Future[Option[User]]

  def finishRegistration(name: String, password: String): Future[String]

  def deleteAdmin(name: String): Future[Unit]

  val checkAuthCookieKey: String
}

case class AuthenticationException() extends Exception("Authentication failed")


//@Singleton
//class DummyCredentialService @Inject() (@NamedCache("session-cache") sessionCache: CacheApi)
//                                       (implicit exec: ExecutionContext) extends CredentialsService {
//
//  val gAuth = new GoogleAuthenticator()
//  val otpKey: GoogleAuthenticatorKey = gAuth.createCredentials()
//  var dummy = new DummyUser("dummy", BCrypt.hashpw("12345678", BCrypt.gensalt(12)), otpKey.getKey)
//
//  val checkAuthCookieKey = "dummyCredToken"
//
//  def createAdmin(name: String, password: String): Future[User] = {
//    Future {
//      dummy
//    }
//  }
//
//  def authAdmin(name: String, password: String, otp: Int) : Future[String] = {
//    Future {
//      if (BCrypt.checkpw(password, dummy.passwordHash) && gAuth.authorize(dummy.otpKey, otp)) {
//        val sessionKey = Random.nextString(64)
//        sessionCache.set(sessionKey, dummy.name, 15.minutes)
//        sessionKey
//      } else {
//        throw AuthenticationException()
//      }
//    }
//  }
//
//  def checkAuth(authKey: String): Option[User] = {
//    sessionCache.get[String](authKey).flatMap {name =>
//      if(dummy.name == name) {
//        sessionCache.set(authKey, name, 15.minutes)
//        Some(dummy)
//      } else {
//        None
//      }
//    }
//  }
//
//  def finishRegistration(name: String, password: String): Future[String] = {
//    Future {
//      if (BCrypt.checkpw(password, dummy.passwordHash)) {
//        GoogleAuthenticatorQRGenerator.getOtpAuthURL("DummyAuth", name, dummy.otpKey)
//      } else {
//        throw AuthenticationException()
//      }
//    }
//  }
//
//}
