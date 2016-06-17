/*
 * Copyright (c) 2016 morpheby
 */

package actors
import akka.actor._

object NodesActor {
  val props = Props[NodesActor]

  case class CreateNode(nodeId: String)

  /* Replies */
}

/**
  * Created by morpheby on 13.6.16.
  */
class NodesActor extends Actor {
  import NodesActor._

  def receive = {
    case CreateNode(nodeId) =>
      sender ! context.actorOf(NodeActor.props(nodeId), nodeId)
  }
}
