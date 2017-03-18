package co.tjcelaya.smack.service.auth.api

import akka.NotUsed
import co.tjcelaya.smack.service.common.LastKnownEnvironment.{getEnv, hasEnv, setEnv}
import co.tjcelaya.smack.service.common.{FriendlyExceptionSerializer, LastKnownEnvironment, VerboseLoggingHeaderFilter}
import com.lightbend.lagom.scaladsl.api.transport._
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.Environment
import play.api.libs.json.{Format, Json}


/**
  * Created by tj on 2/27/17.
  */
trait AuthService extends Service {

  def token(grant_type: String,
            client_id: String,
            client_secret: Option[String],
            username: Option[String],
            password: Option[String],
            refresh_token: Option[String]): ServiceCall[NotUsed, OAuth2AccessToken]

  def jwt(grant_type: String,
          client_id: String,
          client_secret: Option[String],
          username: Option[String],
          password: Option[String],
          refresh_token: Option[String]): ServiceCall[NotUsed, String]

  val tokenParams = Seq("grant_type", "client_id", "client_secret", "username", "password", "refresh_token")

  override final def descriptor: Descriptor = {
    import Service._
    if (!hasEnv) {
      setEnv(Environment.simple())
    }
    val env = getEnv

    named("auth").withCalls(
      //      restCall(Method.POST, "/api/auth/token?" + tokenParams.mkString("&"), token _)
      pathCall("/api/auth/jwt?" + tokenParams.mkString("&"), jwt _),
      pathCall("/api/auth/token?" + tokenParams.mkString("&"), token _)
      //        pathCall("/api/auth/token", token _)
    )
      .withAutoAcl(true)
      .withHeaderFilter(
        HeaderFilter.composite(
          new VerboseLoggingHeaderFilter("UserService"),
          UserAgentHeaderFilter // default
        )
      )
      .withExceptionSerializer(new FriendlyExceptionSerializer(LastKnownEnvironment.getEnv))
  }
}

case class OAuth2AccessToken(access_token: String,
                             token_type: String,
                             expires_in: Option[Long],
                             refresh_token: Option[String],
                             scope: Option[Seq[String]])

object OAuth2AccessToken {
  /**
    * Format for converting greeting messages to and from JSON.
    *
    * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
    */
  implicit val format: Format[OAuth2AccessToken] = Json.format[OAuth2AccessToken]
}

//final abstract class GrantType {
//  val CLIENT_CREDENTIALS = "client_credentials"
//}

case class BadGrantTypeException(grantType: String)
  extends TransportException(TransportErrorCode.BadRequest, s"grant_type provided is invalid or unsupported: $grantType")

case class InvalidClientSecretException()
  extends TransportException(TransportErrorCode.BadRequest, s"client_secret must be provided")

case class InvalidUsernameException()
  extends TransportException(TransportErrorCode.BadRequest, s"username must be provided")

case class InvalidPasswordException()
  extends TransportException(TransportErrorCode.BadRequest, s"Password must be provided")

