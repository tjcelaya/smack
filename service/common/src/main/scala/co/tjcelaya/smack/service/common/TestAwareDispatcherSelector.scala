package co.tjcelaya.smack.service.common

import akka.actor.ActorSystem
import play.api.Environment
import scala.concurrent.ExecutionContext

/**
  * Created by tj on 3/25/17.
  */
trait TestAwareDispatcherSelector {

  protected def dispatcher(implicit environment: Environment, system: ActorSystem) =
    if (environment.mode.equals(play.api.Mode.Test))
      ExecutionContext.Implicits.global
    else
      system.dispatcher
}
