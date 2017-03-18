package co.tjcelaya.smack.state

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import co.tjcelaya.smack.state.example.traffic.TrafficLight
import com.typesafe.scalalogging.Logger

import scala.util.Random

/**
  * Created by tj on 2/12/17.
  */
object State {
  private val logger = Logger(State.getClass)

  def main(args: Array[String]) {
    logger.info("booting actor system")
    implicit val system = ActorSystem(sys.env.getOrElse("ACTOR_SYSTEM", "actor-system"))
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    // val props2 = Props(new ActorWithArgs("arg")) // careful, see below
    // val t0 = system.actorOf(Props[TrafficLight], Random.alphanumeric.take(10).mkString)

    val t0a = system.actorOf(Props[TrafficLight], "t0")


    sys.addShutdownHook({
      logger.info("shutdown hook triggered, shutting down actor system")
      system.terminate()
    })
  }
}
