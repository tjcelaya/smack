package co.tjcelaya.smack.service.common

import java.util.concurrent.atomic.AtomicReference

import play.api.Environment
/**
  * Created by tj on 2/22/17.
  */
object LastKnownEnvironment {
  private val env = new AtomicReference[Environment]
  def hasEnv: Boolean = env.get() != null
  def getEnv: Environment = env.get()
  def setEnv(environment: Environment): Unit = env.set(environment)
}
