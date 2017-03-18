package co.tjcelaya.smack.state.example.traffic

import akka.actor.ActorSystem
import org.scalatest._
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}

import scala.concurrent.duration._
import scala.concurrent.Await
import akka.pattern.ask
import co.tjcelaya.smack.state.example.traffic.TrafficLight.NeighborList

import scala.util.Success

/**
  * Created by tj on 2/12/17.
  */
class TrafficLightSpec
  extends TestKit(ActorSystem("testSystem"))
    with FlatSpecLike
    with Matchers
    with ImplicitSender
    with DefaultTimeout
    with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  it must "be stopped by default" in {
    val tL = TestActorRef(new TrafficLight)
    tL ! GetState
    expectMsg(Stop)
  }

  it must "have a ToString" in {
    val tL = TestActorRef(new TrafficLight)
    tL ! ToString
    expectMsgType[String]
  }

  it must "accept and honor SyncRequest" in {
    val tL = TestActorRef(new TrafficLight)
    tL ! SyncRequest(Opposite, Go)
    tL ! GetState
    expectMsg(Go)
  }

  it must "accept GetNeighborCount" in {
    val tL0 = TestActorRef(new TrafficLight)
    tL0 ! GetNeighborCount
    expectMsg(0)
  }

  it must "accept and honor NeighborAssociation" in {
    val tL0 = TestActorRef(new TrafficLight)
    val tL1 = TestActorRef(new TrafficLight)
    tL0 ! AssociateNeighbor(Opposite, tL1)
    tL0 ! GetNeighborCount
    expectMsg(1)
    val neighborsFuture = tL0 ? GetNeighbors
    val Success(ns: NeighborList) = neighborsFuture.value.get
    assert(ns.contains(Opposite))
    assert(ns(Opposite) == tL1)
  }

  it must "connect back" in {
    val tLN = TestActorRef(new TrafficLight)
    val tLS = TestActorRef(new TrafficLight)

    tLS ! AssociateNeighbor(Opposite, tLN)
    tLS ! GetNeighborCount
    expectMsg(1)

    tLN ! GetNeighborCount
    expectMsg(1)
  }

  it must "invert Right correctly" in {
    val tLS = TestActorRef(new TrafficLight, "Si1")
    val tLE = TestActorRef(new TrafficLight, "Ei1")
    tLS ! AssociateNeighbor(Right, tLE)
    tLS ! GetNeighborCount
    expectMsg(1)

    val neighborsFuture = tLE ? GetNeighbors
    val Success(ns: NeighborList) = neighborsFuture.value.get
    assert(ns.contains(Left))
    assert(ns(Left) == tLS)
  }

  it must "invert Left correctly" in {
    val tLS = TestActorRef(new TrafficLight, "Si2")
    val tLE = TestActorRef(new TrafficLight, "Ei2")
    tLE ! AssociateNeighbor(Left, tLS)
    tLE ! GetNeighborCount
    expectMsg(1)

    val neighborsFuture = tLS ? GetNeighbors
    val Success(ns: NeighborList) = neighborsFuture.value.get
    assert(ns.contains(Right))
    assert(ns(Right) == tLE)
  }

  it can "gossip connections to existing neighbors" in {
    val tLN = TestActorRef(new TrafficLight, "N2")
    val tLE = TestActorRef(new TrafficLight, "E2")
    val tLS = TestActorRef(new TrafficLight, "S2")

    tLS ! AssociateNeighbor(Opposite, tLN)
    tLS ! AssociateNeighbor(Right, tLE)

    tLE ! GetNeighborCount
    expectMsg(2)

    tLE ! GetNeighborCount
    expectMsg(2)

    tLN ! GetNeighborCount
    expectMsg(2)
  }

  it can "flip directions" in {
    assert(Opposite == TrafficLight.translateNeighborDirection(Opposite, Self))
    assert(Right == TrafficLight.translateNeighborDirection(Left, Opposite))
    assert(Left == TrafficLight.translateNeighborDirection(Right, Opposite))

    assert(Right == TrafficLight.translateNeighborDirection(Opposite, Left))
    assert(Left == TrafficLight.translateNeighborDirection(Opposite, Right))

    val adjacemtDirectionList = List(Opposite, Left, Right)
    adjacemtDirectionList.foreach(originDir => {
      adjacemtDirectionList.foreach(relationDir => {
        val d = TrafficLight.translateNeighborDirection(originDir, relationDir)
        assert(d != Indirect, s"origin: $originDir, rel: $relationDir")
        assert(d != Tail)
      })
    })
  }
}