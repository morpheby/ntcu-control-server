/*
 * Copyright (c) 2016 morpheby
 */

package dao

import models.Node
import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by morpheby on 12.6.16.
  */
class NodeDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
                        (implicit exec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  def get(id: String): Node = {
    Node("testNode", None, List("ntcu-xdevice"))
  }
}
