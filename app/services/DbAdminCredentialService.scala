/*
 * Copyright (c) 2016 morpheby
 */

package services

import javax.inject.{Inject, Singleton}

import com.warrenstrange.googleauth.{GoogleAuthenticator, GoogleAuthenticatorKey, GoogleAuthenticatorQRGenerator}
import models.User
import dao.UserDAO
import org.mindrot.jbcrypt.BCrypt
import play.api.cache.{CacheApi, _}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random
import scala.concurrent.duration._
import services.AdminAuthExceptions._

/**
  * Created by morpheby on 28.5.16.
  */
@Singleton
class DbAdminCredentialService @Inject()(@NamedCache("session-cache") sessionCache: CacheApi)
                                        (userDao: UserDAO)
                                        (implicit exec: ExecutionContext) extends AdminCredentialsService {
    val gAuth = new GoogleAuthenticator()
    val checkAuthCookieKey = "controlCredentials"

    def createAdmin(name: String, password: Option[String], authKey: Option[String]): Future[User] = {
    userDao.get(name).flatMap {
        case None =>
          // Create user and insert to table
          (authKey.map(checkAuth(_)) getOrElse { Future.successful(None) }).flatMap {
            _.map { authUser =>
              val user = User(name, password, None)
              userDao.insert(user).map(_ => user)
            } getOrElse {
              throw AdminAuthenticationException()
            }
          }
        case Some(user @ User(_, None, None)) =>
          // Fill password value
          val newUser = User(user.name, password.map(BCrypt.hashpw(_, BCrypt.gensalt(12))), None)
          userDao.update(newUser).map(_ => newUser)
        case Some(user @ User(_, _, _)) =>
          // User does not require registration
          Future.failed(InvalidOperationException("User already exists"))
      }
    }

    def deleteAdmin(name: String): Future[Unit] = {
      if (name == "admin") {
        userDao.update(User(name, None, None))
      } else {
        userDao.delete(User(name, None, None))
      }
    }

    def authAdmin(name: String, password: String, otp: Int) : Future[String] = {
      userDao.get(name).flatMap {
        case Some(user @ User(_, Some(pwHash), Some(otpKey))) =>
          if (BCrypt.checkpw(password, pwHash) && gAuth.authorize(otpKey, otp)) {
            val sessionKey = Random.nextString(64)
            sessionCache.set(sessionKey, user.name, 15.minutes)
            Future.successful(sessionKey)
          } else {
            Future.failed(AdminAuthenticationException())
          }
        case None =>
          Future.failed(AdminAuthenticationException())
        case _ =>
          Future.failed(InvalidInputException())
      }
    }

    def checkAuth(authKey: String): Future[Option[User]] = {
      sessionCache.get[String](authKey).map { name =>
        userDao.get(name).flatMap {
          case None | Some(User(_, _, None)) | Some(User(_, None, None)) =>
            Future.successful(None)
          case Some(user: User) =>
            // Update session
            sessionCache.set(authKey, name, 15.minutes)
            Future.successful(Some(user))
        }
      } getOrElse {
        Future.failed(SessionExpired())
      }
    }

    def finishRegistration(name: String, password: String): Future[String] = {
      userDao.get(name).flatMap {
        case Some(user @ User(_, Some(pwHash), None)) =>
          if (BCrypt.checkpw(password, pwHash)) {
            val key = gAuth.createCredentials()
            val url = GoogleAuthenticatorQRGenerator.getOtpAuthURL("NTC-U Control Server", name, key)
            val newUser = User(user.name, user.passwordHash, Some(key.getKey))
            userDao.update(newUser).map(_ => url)
          } else {
            Future.failed(AdminAuthenticationException())
          }
        case Some(User(_, _, Some(_))) =>
          Future.failed(InvalidOperationException("User already finished registration"))
        case None =>
          Future.failed(AdminAuthenticationException())
        case _ =>
          Future.failed(InvalidInputException())
      }
    }


  }
