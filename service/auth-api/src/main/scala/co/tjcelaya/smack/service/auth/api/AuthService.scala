package co.tjcelaya.smack.service.auth.api

import akka.{Done, NotUsed}
import co.tjcelaya.smack.service.common.LastKnownEnvironment.{getEnv, hasEnv, setEnv}
import co.tjcelaya.smack.service.common.{FriendlyExceptionSerializer, LastKnownEnvironment, OptionsServiceCallGenerator, VerboseLoggingHeaderFilter}
import com.lightbend.lagom.scaladsl.api.transport._
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import com.typesafe.scalalogging.slf4j.LazyLogging
import play.api.Environment
import play.api.libs.json.{Format, JsArray, Json}


/**
  * Created by tj on 2/27/17.
  */
trait AuthService extends Service with LazyLogging {

  def options: ServiceCall[NotUsed, JsArray] = OptionsServiceCallGenerator(descriptor)

  def token(grant_type: String,
            client_id: String,
            client_secret: Option[String] = None,
            username: Option[String] = None,
            password: Option[String] = None,
            refresh_token: Option[String] = None): ServiceCall[NotUsed, OAuth2AccessToken]

  def jwt(grant_type: String,
          client_id: String,
          client_secret: Option[String],
          username: Option[String],
          password: Option[String],
          refresh_token: Option[String]): ServiceCall[NotUsed, String]

  def checkToken: ServiceCall[String, Boolean]

  def registerClient: ServiceCall[Client, Done]

  def showClient(id: String): ServiceCall[NotUsed, String]

  // you shouldn't need to check a jwt, it's signed!
  // def checkJwt: ServiceCall[String, Boolean]

  val tokenParams = Seq("grant_type", "client_id", "client_secret", "username", "password", "refresh_token")

  override final def descriptor: Descriptor = {
    import Service._
    if (!hasEnv) {
      setEnv(Environment.simple())
    }
    val env = getEnv

    named("auth").withCalls(
      restCall(Method.OPTIONS, "/", options _),
      restCall(Method.OPTIONS, "/api/auth", options _),
      //      restCall(Method.POST, "/api/auth/token?" + tokenParams.mkString("&"), token _)
      pathCall("/api/auth/jwt?" + tokenParams.mkString("&"), jwt _),
      pathCall("/api/auth/token?" + tokenParams.mkString("&"), token _),
      pathCall("/api/auth/check", checkToken _),
      restCall(Method.POST, "/api/auth/client", registerClient _),
      restCall(Method.GET, "/api/auth/client/:id", showClient _)
      //        pathCall("/api/auth/token", token _)
    )
      .withAutoAcl(true)
      .withHeaderFilter(
        HeaderFilter.composite(
          new VerboseLoggingHeaderFilter("UserService"),
          UserAgentHeaderFilter // default
        )
      )
      .withExceptionSerializer(new FriendlyExceptionSerializer(getEnv))
  }
}