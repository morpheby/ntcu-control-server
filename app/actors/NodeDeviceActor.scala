/*
 * Copyright (c) 2016 morpheby
 */

package actors

import akka.actor._

object NodeDeviceActor {
  def props(deviceId: String) = Props(classOf[NodeDeviceActor], deviceId)


}

/**
  * Created by morpheby on 11.6.16.
  */
class NodeDeviceActor(deviceId: String) extends Actor {
  import NodeDeviceActor._

  // The parent Node
  val parent = context.parent

  def receive = {
    case msg => parent forward msg
  }

}
