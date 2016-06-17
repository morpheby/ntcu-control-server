/*
 * Copyright (c) 2016 morpheby
 */

package controllers

import akka.actor.ActorSystem
import javax.inject._

import actions.AdminAuthAction
import play.api._
import play.api.mvc._
import services._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration._
import play.api.libs.functional._
import play.api.libs.json._

/**
  * Created by morpheby on 6/17/16.
  */
@Singleton
class NodeController @Inject()(actorSystem: ActorSystem,
                               authAction: AdminAuthAction, credentialService: AdminCredentialsService) {

}
