/*
 * Copyright (c) 2016 morpheby
 */

package dao

import javax.inject.Inject

import models.User
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by morpheby on 28.5.16.
  */
class UserDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
                        (implicit exec: ExecutionContext)
      extends HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private val Users = TableQuery[UserTable]

  def get(name: String): Future[Option[User]] = {
    db.run(Users.filter(_.name === name).result).map(_.headOption)
  }

  def insert(user: User): Future[Unit] = {
    db.run(Users += user).map(_ => ())
  }

  def update(user: User): Future[Unit] = {
    db.run(Users.filter(_.name === user.name).update(user)).map(_ => ())
  }

  def delete(user: User): Future[Unit] = {
    db.run(Users.filter(_.name === user.name).delete).map(_ => ())
  }

  private class UserTable(tag: Tag) extends Table[User](tag, "USER") {
    def name = column[String]("NAME", O.PrimaryKey)
    def password = column[Option[String]]("PASSWORD")
    def otpKey = column[Option[String]]("OTPKEY")

    def * = (name, password, otpKey) <> (User.tupled, User.unapply)
  }


}
