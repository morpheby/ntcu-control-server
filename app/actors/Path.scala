/*
 * Copyright (c) 2016 morpheby
 */

package actors

import akka.actor.{ActorPath, RootActorPath}

/**
  * Created by morpheby on 13.6.16.
  */
object Paths {
  val rootPath = ActorPath.fromString("/user")

  val nodes = "nodes"
  val nodesPath = rootPath/nodes

}
