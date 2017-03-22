package co.tjcelaya.smack.service.user.api

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.Source
import co.tjcelaya.smack.service.common.seq.Paginator
import co.tjcelaya.smack.service.common.{FriendlyExceptionSerializer, OptionsServiceCallGenerator, VerboseLoggingHeaderFilter}
import com.google.inject.Inject
import com.lightbend.lagom.scaladsl.api.transport.{HeaderFilter, UserAgentHeaderFilter}
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import com.typesafe.scalalogging.Logger
import com.typesafe.scalalogging.slf4j.LazyLogging
import play.api.libs.json._
import play.api.Environment

import scala.collection.immutable.Seq

trait UserService
  extends Service
  with LazyLogging {

  def descriptor: Descriptor = {
    import Service._
    import co.tjcelaya.smack.service.common.LastKnownEnvironment._
    import com.lightbend.lagom.scaladsl.api.transport.Method

    if (!hasEnv) {
      setEnv(Environment.simple())
    }
    val env = getEnv

    named("user")
      .withLocatableService(true)
      .withCalls(
        restCall(Method.OPTIONS, "/", options _),
        restCall(Method.OPTIONS, "/api/user", options _),
        restCall(Method.POST, "/api/user", createUser _),
        restCall(Method.GET, "/api/user/:id", getUser _),
        restCall(Method.GET, "/api/user?page&pageSize", getUsersPage _),
        restCall(Method.GET, "/api/user-read", getUsersFromReadSide _),
        restCall(Method.GET, "/api/user-write", getUsersFromWriteSide _)
      )
      .withAutoAcl(true)
      .withHeaderFilter(
        HeaderFilter.composite(
          new VerboseLoggingHeaderFilter(this.getClass.getSimpleName),
          UserAgentHeaderFilter // default
        )
      )
      .withExceptionSerializer(new FriendlyExceptionSerializer(env))
  }

  def options: ServiceCall[NotUsed, JsArray] = OptionsServiceCallGenerator(descriptor)

  def createUser: ServiceCall[CreateUser, User]

  def getUser(userId: UUID): ServiceCall[NotUsed, User]

  // TODO: paginate properly and restrict to admin scope
  def getUsers: ServiceCall[NotUsed, Seq[User]]

  def getUsersPage(page: Option[Int] = Some(1), pageSize: Option[Int] = Some(10)): ServiceCall[NotUsed, Paginator[User]]

  def getUsersFromReadSide: ServiceCall[NotUsed, Seq[User]]

  def getUsersFromWriteSide: ServiceCall[NotUsed, Seq[User]]
}
