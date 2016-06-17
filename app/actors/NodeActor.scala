/*
 * Copyright (c) 2016 morpheby
 */

package actors

import javax.inject.Inject

import akka.actor._
import dao.NodeDAO


object NodeActor {
  def props(nodeId: String) = Props(classOf[NodeActor], nodeId)

  case class Ping()

  /* Device-side requests */

  /** Puts resource request in a queue */
  case class RequestResource(name: String)

  /** Asks to allocate. If not available -- doesn't respond, otherwise, responds with MayAlloc */
  case class RequestAlloc()

  /** Confirms that allocation is still required and allows to proceed */
  case class ConfirmAlloc()

  /** Tells that allocation is not required anymore */
  case class AllocNotNeeded()

  /** Frees allocated node */
  case class Free()

  /* Client-side requests */

  /** Puts resource data */
  case class PutResource[T](resource: models.Resource[T])

  /** Confirms to the actor that node client is available and responding */
  case class ConfirmAllocAvailable()

  /* Replies */

  /** Tells that node is available for allocation. Requires fast response with either ConfirmAlloc or AllocNotNeeded */
  case class MayAlloc(actorRef: ActorRef, group: Option[String])
}

case class InvalidNodeActionException(message: String) extends Exception

/**
  * Created by morpheby on 11.6.16.
  */
class NodeActor @Inject() (nodeDAO: NodeDAO)(nodeId: String) extends Actor {
  import NodeActor._

  def node: models.Node = nodeDAO.get(nodeId)

  val deviceActorRefs: Seq[ActorRef] = node.devices map { deviceId => context.actorOf(NodeDeviceActor.props(deviceId), deviceId) }


  object State {
    class State {}
    case class Allocated(by: ActorRef, actions: Seq[models.Action]) extends State
    case class Free() extends State
    case class RequestedAllocation(by: Seq[ActorRef]) extends State
    case class AwaitingAllocConfirmation(by: ActorRef, others: Seq[ActorRef]) extends State
  }

  var state: State.State = State.Free()
  var allocatedBy: Option[ActorRef] = None

  def receive = {
    case Ping() =>
      sender ! Ping()

    case RequestAlloc() =>
      state match {
        case State.RequestedAllocation(refs) =>
          state = State.RequestedAllocation(refs :+ sender)
        case State.Free() =>
          state = State.RequestedAllocation(sender :: Nil)
        case State.AwaitingAllocConfirmation(a, refs) =>
          state = State.AwaitingAllocConfirmation(a, refs :+ sender)
        case _ =>
      }

    case ConfirmAllocAvailable() =>
      state match {
        case State.RequestedAllocation(selected :: refs) =>
          state = State.AwaitingAllocConfirmation(selected, refs)
          selected ! MayAlloc(self, node.group)
        case _ =>
      }

    case ConfirmAlloc() =>
      state match {
        case State.AwaitingAllocConfirmation(s, _) if s == sender =>
          state = State.Allocated(sender, Nil)
        case _ =>
          throw InvalidNodeActionException(s"Allocation is not allowed anymore. Inconsistent state for $sender")
      }

    case AllocNotNeeded() =>
      state match {
        case State.AwaitingAllocConfirmation(s, next :: others) if s == sender =>
          state = State.AwaitingAllocConfirmation(next, others)
          next ! MayAlloc(self, node.group)
        case _ =>
          throw InvalidNodeActionException(s"Allocation is not allowed anymore. Inconsistent state for $sender")
      }

    case Free() =>
      state match {
        case State.Allocated(s, Nil) if s == sender =>
          state = State.Free()
        case _ =>
          throw InvalidNodeActionException(s"Inconsistent state for $sender on $self")
      }

  }
}
