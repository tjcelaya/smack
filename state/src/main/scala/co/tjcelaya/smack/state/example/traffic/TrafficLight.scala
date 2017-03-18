package co.tjcelaya.smack.state.example.traffic

/**
  * Created by tj on 2/12/17.
  */

import akka.actor.{Actor, ActorRef}
import akka.event.{Logging, LoggingAdapter}
import co.tjcelaya.smack.state.example.traffic.TrafficLight.NeighborList
import scala.util.Random
import akka.event.Logging

case object GetState
case object GetNeighborCount
case object GetNeighbors
case object ToString
final case class AssociateNeighbor(direction: NeighborDirection, actorRef: ActorRef, ack: Boolean = false)
final case class SendGossip(direction: NeighborDirection, neighborList: NeighborList, ack: Boolean = false)
final case class SyncRequest(neighborDirection: NeighborDirection, trafficLightState: TrafficLightState)

object TrafficLight {
  type NeighborList = Map[NeighborDirection, ActorRef]

  def translateNeighborDirection(origin: NeighborDirection, neighborDirection: NeighborDirection): NeighborDirection = {
    origin match {
      case Self => origin
      case Opposite =>
        neighborDirection match {
          case Self => Opposite
          case Opposite => Self
          case Tail => Indirect
          case Left => Right
          case Right => Left
          case Indirect => Indirect
        }
      case Tail =>
        neighborDirection match {
          case Self => Tail
          case Opposite => Indirect
          case Tail => Tail
          case Left => Indirect
          case Right => Indirect
          case Indirect => Indirect
        }
      case Left =>
        neighborDirection match {
          case Self => Left
          case Opposite => Right
          case Tail => Indirect
          case Left => Opposite
          case Right => Self
          case Indirect => Indirect
        }
      case Right =>
        neighborDirection match {
          case Self => Right
          case Opposite => Left
          case Tail => Indirect
          case Left => Self
          case Right => Opposite
          case Indirect => Indirect
        }
      case Indirect => Indirect
    }
  }

  def shortIdent(o: ActorRef): String = {
    o.path.name
  }

  def translateNeighborList(
    originNeighbor: Option[ActorRef],
    originDirection: NeighborDirection,
    neighborList: Map[NeighborDirection, ActorRef]
  ): Map[NeighborDirection, ActorRef] = {
    def normalizeDirections = {
      neighborList.map({ case (d, r) => (translateNeighborDirection(originDirection, d), r) })
    }

    originNeighbor match {
      case Some(n) => normalizeDirections + (originDirection -> n)
      case None => normalizeDirections
    }
  }

  def renderNeighborList(neighborList: NeighborList, selfRef: Option[ActorRef] = None): String = {
    def r(sym: String, direction: NeighborDirection, padding: String = ""): String = {
      if (neighborList.contains(direction))
        s"$sym${shortIdent(neighborList(direction))}"
      else
        padding
    }

    List(
      s" ${r("O", Opposite)}",
      r("L", Left, "    ") + " " + r("R", Right),
      selfRef match {
        case Some(ref) => s" S${shortIdent(ref)} (p) "
        case None if neighborList.contains(Self) => s" S${shortIdent(neighborList(Self))} (m) "
        case None => ""
      },
      s" ${r("T", Tail)}",
      r("I", Indirect)
    ).mkString("\n")
  }

  var shouldLog = false

  def logGuarded(logger: LoggingAdapter, msg: String, ref: ActorRef): Unit = {
    if (shouldLog)
      if (true) {
        println(s"from ${shortIdent(ref)}: $msg")
      } else {
        logger.error(msg)
      }
  }
}

/**
  * Traffic Light Actor, rear is the light at the other end
  * of the street if the street is a snake, this is at the head
  * and rear is at the tail if the traffic light were a snake's head
  */
class TrafficLight
  extends Actor {

  import TrafficLight._

  private val logger = Logging(context.system, this)
  private var state: TrafficLightState = Stop
  private var neighbors: Map[NeighborDirection, ActorRef] = Map.empty[NeighborDirection, ActorRef]

  def log(msg: String)(implicit ref: ActorRef): Unit = {
    logGuarded(logger, msg, ref)
  }

  override def receive: PartialFunction[Any, Unit] = {
    case AssociateNeighbor(direction, ref, ack) if neighbors.contains(direction) && neighbors(direction) == sender() =>
      log(s"saw duplicate association from d: $direction, r: ${shortIdent(sender())}")
    case AssociateNeighbor(direction, ref, ack) if neighbors.contains(direction) =>
      log(s"known association received, checking identity")
      if (List(Self, Indirect).contains(direction)) {
        log("shouldn't add this")
      } else if (neighbors(direction) != ref) {
        throw new Exception(s"unexpected ref, replacing! direction: $direction, r: ${shortIdent(ref)}, " +
          s"neighbors: \n${renderNeighborList(neighbors)}")
      } else {
        addNeighbor(direction, ref)
      }

    case AssociateNeighbor(direction, ref, ack) =>
      log(s"association received: $direction, ack: $ack, sender: ${shortIdent(sender())}")

      log(s"updating map")
      if (List(Self, Indirect).contains(direction)) {
        log("shouldn't add this ")
      } else {
        addNeighbor(direction, ref)
      }
      log(s"neighbors updated: (self provided) \n${renderNeighborList(neighbors, Some(self))}")

      if (!ack) {
        log(s"saw unack so returning association")
        ref ! AssociateNeighbor(direction.inverse, self, ack = true)
      } else {
        log(s"saw ack so sending gossip")
        ref ! SendGossip(direction.inverse, neighbors)
      }

    case SendGossip(direction, neighborList, ack) =>
      log(s"receiving gossip, direction: $direction, ack: $ack, sender: ${shortIdent(sender())}, neighborList: \n${renderNeighborList(neighborList)}")
      var nL = translateNeighborList(Some(sender()), direction, neighborList)
      log(s"translated to my perspective (${nL.size}): \n${renderNeighborList(nL)}")

      if (nL.contains(Self) && nL(Self) != self) {
        log(s"renormalized neighbor map does not have self set correctly, quitting?")
        context.system.stop(self)
      } else {
        nL = nL - Self
      }

      if (nL.contains(Indirect)) {
        nL = nL - Indirect
      }

      if (nL(direction) == neighbors(direction) && nL(direction) == sender()) {
        log(s"removing known sender ${sender().path.name} from gossip map")
        nL = nL - direction
      }

      log(s"sanitized: (${nL.size}): \n${renderNeighborList(nL)}")

      val shouldShare = (neighbors.toSet diff nL.toSet).toMap
      val shouldAssociate = (nL.toSet diff neighbors.toSet).toMap

      log(s"shouldShare: (${shouldShare.size}): \n${renderNeighborList(shouldShare)}")
      log(s"shouldAssociate: (${shouldAssociate.size}): \n${renderNeighborList(shouldAssociate)}")
      log(s"known: (${neighbors.size}): \n${renderNeighborList(neighbors)}")

      log(s"ffc: ${neighborList.size}, nc: ${neighbors.size}, ssc: ${shouldShare.size}")

      log(s"sending ${shouldAssociate.size} callbacks")

      shouldAssociate foreach {
        case (dir: NeighborDirection, nRef: ActorRef) => {
          log("telling new neighbor to add self")
          addNeighbor(dir, nRef)
          nRef ! AssociateNeighbor(dir.inverse, self)
        }
        case _ => log("???")
      }

      if (!ack) {
        log("saw unacked gossip")
        if (0 == shouldShare.size) {
          log("but have nothing to share")
        } else {
          log("replying to gossip")
          sender() ! SendGossip(direction.inverse, shouldShare, ack = true)
        }
      }
    case SyncRequest(direction, newState) =>
      log("sync received")

      val targetState = if (List(Left, Right) contains direction)
        if (newState == Stop) Go else Stop
      else
        newState

      val neighborOptions = List(Left, Right, Opposite, Tail).filter(d => neighbors.contains(d) && d != direction)
      if (0 < neighborOptions.length) {
        val neighborDirectionChosen = neighborOptions(Random.nextInt(neighborOptions.length))
        neighbors(neighborDirectionChosen) ! SyncRequest(neighborDirectionChosen.inverse, newState)
      }

      state = targetState
    case GetState =>
      sender() ! state.clone
    case GetNeighborCount =>
      sender() ! neighbors.size
    case GetNeighbors =>
      sender() ! neighbors.clone
    case ToString =>
      sender() ! s"base = ${super.toString}, state = $state, neighbors = $neighbors"
    case m @ _ =>
      val msg = s"unknown message received: $m"
      log(msg)
  }

  private def addNeighbor(direction: NeighborDirection, ref: ActorRef) = {
    if (0 < neighbors.count(_._2 == ref)) {
      throw new Exception(
        s"adding an already existing neighbor d:$direction, r: ${ref.path.name}, nL: \n" +
          renderNeighborList(neighbors)
      )
    }

    neighbors = neighbors + (direction -> ref)
  }
}
