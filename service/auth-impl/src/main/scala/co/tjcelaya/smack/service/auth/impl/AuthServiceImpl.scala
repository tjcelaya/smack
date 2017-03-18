package co.tjcelaya.smack.service.auth.impl

import akka.NotUsed
import akka.actor.ActorSystem
import co.tjcelaya.smack.service.auth.api._
import co.tjcelaya.smack.service.common.{DateFactory, MultiException}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.TransportException
import com.typesafe.scalalogging.slf4j.LazyLogging
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtJson}

import scala.concurrent.Future
import scalaoauth2.provider._

/**
  * Implementation of the AuthService.
  */
class AuthServiceImpl(system: ActorSystem,
                      accessTokenRepository: AccessTokenRepository,
                      userOAuth2Provider: UserOAuth2Provider)
  extends AuthService
    with LazyLogging {

  OAuth2ProviderResolver.registerProvider(User.getClass, userOAuth2Provider)
  logger.error("booting " + this.getClass.toString)

  override def finalize(): Unit = logger.error("terminating " + this.getClass.toString); super.finalize()

  val tokenEndpoint = new TokenEndpoint {
    override val handlers = Map(
      OAuthGrantType.CLIENT_CREDENTIALS -> new ClientCredentials
    )
  }

  override def jwt(grant_type: String,
                   client_id: String,
                   client_secret: Option[String],
                   username: Option[String],
                   password: Option[String],
                   refresh_token: Option[String]): ServiceCall[NotUsed, String] = ServiceCall { _ =>
    token(grant_type, client_id, client_secret, username, password, refresh_token)
      .invoke()
      .flatMap[String] { t: OAuth2AccessToken =>
      val epochSecond = DateFactory.now.toInstant.getEpochSecond
      Future.successful(
        JwtJson.encode(
          JwtClaim(
            "{}",
            Some(this.getClass.toGenericString),
            Some("subject"),
            Some(Set("audience")),
            t.expires_in match {
              case None => None
              case Some(secondsUntilExpiration) => Some(epochSecond + secondsUntilExpiration)
            },
            Some(epochSecond),
            Some(epochSecond),
            Some("jwtid")
          ),
          "key",
          JwtAlgorithm.HS256))
      Future.successful(t.toString)
    }(system.dispatcher)
  }

  override def token(grant_type: String,
                     client_id: String,
                     client_secret: Option[String],
                     username: Option[String],
                     password: Option[String],
                     refresh_token: Option[String]): ServiceCall[NotUsed, OAuth2AccessToken] = ServiceCall { _ =>
    val m = Map(
      "grant_type" -> Seq(grant_type),
      "client_id" -> Seq(client_id)
    )

    var errors: List[TransportException] = List[TransportException]()
    val noParams = Map.empty[String, Seq[String]]
    val added: Map[String, Seq[String]] = grant_type match {
      case OAuthGrantType.AUTHORIZATION_CODE => noParams
      case OAuthGrantType.REFRESH_TOKEN => noParams
      case OAuthGrantType.CLIENT_CREDENTIALS =>
        client_secret match {
          case None =>
            errors = errors.::(InvalidClientSecretException())
            noParams
          case Some(secret) =>
            Map[String, Seq[String]](("client_secret", Seq(secret)))
        }
      case OAuthGrantType.PASSWORD =>
        (username, password) match {
          case (None, None) |
               (None, _) |
               (_, None) =>
            if (username.isEmpty) {
              errors = errors.::(InvalidUsernameException())
            }
            if (password.isEmpty) {
              errors = errors.::(InvalidPasswordException())
            }
            noParams
          case (Some(user), Some(pass)) =>
            Map[String, Seq[String]](("username", Seq(user)), ("password", Seq(pass)))
        }
      case OAuthGrantType.IMPLICIT =>
        noParams
      case _ => noParams
    }

    val r = added match {
      case paramsMissing: Map[String, Seq[String]] if paramsMissing == Map.empty[String, Seq[String]] =>
        Future.failed(MultiException(errors))
      case params: Map[String, Seq[String]] =>
        tokenEndpoint.handleRequest(
          new AuthorizationRequest(Map(), m ++ added), OAuth2ProviderResolver.lookup(User.getClass)
        )(system.dispatcher)
          .flatMap[OAuth2AccessToken] {
          case Left(err: OAuthError) =>
            throw err
          case Right(grantHandlerResult: GrantHandlerResult[_]) =>
            Future.successful(OAuth2AccessToken(
              grantHandlerResult.accessToken,
              grantHandlerResult.tokenType,
              grantHandlerResult.expiresIn,
              grantHandlerResult.refreshToken,
              grantHandlerResult.scope.map(_.split(" ").toSeq)))
        }(system.dispatcher)
    }

    r
  }
}


//object Mode extends Enumeration {
//  type Mode = Value
//  val Dev, Test, Prod = Value
//}