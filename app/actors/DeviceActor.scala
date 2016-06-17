/*
 * Copyright (c) 2016 morpheby
 */

package actors

import akka.actor._

object DeviceActor {
  def props(deviceId: String) = Props(classOf[DeviceActor], deviceId)

  /* Replies */
  case class DeviceReady()
}

/**
  * Created by morpheby on 11.6.16.
  */
class DeviceActor(deviceId: String) extends Actor {
  import DeviceActor._

  val parent = context.parent

  object State {
    class State {}
    case class StartingUp() extends State
    case class Ready(deviceActor: NodeDeviceActor) extends State
  }

  var state: State.State = State.StartingUp()

  override def preStart() = {
    context.actorSelection(Paths.nodesPath/"*"/deviceId) ! NodeActor.RequestAlloc()
  }

  def receive = {
    case _ =>
  }
}
