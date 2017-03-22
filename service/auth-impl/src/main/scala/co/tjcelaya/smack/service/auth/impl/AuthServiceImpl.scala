package co.tjcelaya.smack.service.auth.impl

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import co.tjcelaya.smack.service.auth.api._
import co.tjcelaya.smack.service.auth.api.exceptions._
import co.tjcelaya.smack.service.auth.impl.entity._
import co.tjcelaya.smack.service.common.{DateFactory, MultiException, TestAwareDispatcherSelector}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.TransportException
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.typesafe.scalalogging.slf4j.LazyLogging
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtJson}
import play.api.Environment

import scala.concurrent.{ExecutionContext, Future}
import scalaoauth2.provider._

/**
  * Implementation of the AuthService.
  */
class AuthServiceImpl(environment: Environment,
                      system: ActorSystem,
                      accessTokenRepository: AccessTokenRepository,
                      oauth2Provider: OAuth2Provider,
                      persistentEntityRegistry: PersistentEntityRegistry)
  extends AuthService
    with TestAwareDispatcherSelector {
  implicit private val env = environment
  implicit private val sys = system

  logger.warn(s"booting ${getClass.getSimpleName}")

  override def finalize(): Unit = {
    logger.warn(s"terminating ${getClass.getSimpleName}")
    super.finalize()
  }


  val tokenEndpoint = new TokenEndpoint {
    override val handlers = Map(
      OAuthGrantType.CLIENT_CREDENTIALS -> new ClientCredentials,
      OAuthGrantType.PASSWORD -> new Password
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
    }(dispatcher)
  }

  override def token(grant_type: String,
                     client_id: String,
                     client_secret: Option[String] = None,
                     username: Option[String] = None,
                     password: Option[String] = None,
                     refresh_token: Option[String] = None): ServiceCall[NotUsed, OAuth2AccessToken] = ServiceCall {
    _ =>
      val m = Map(
        "grant_type" -> Seq(grant_type),
        "client_id" -> Seq(client_id)
      )

      var errors: List[TransportException] = List[TransportException]()
      val noParams = Map.empty[String, Seq[String]]
      val authFlow: (AuthType.AuthType, Map[String, Seq[String]]) = grant_type match {
        case OAuthGrantType.AUTHORIZATION_CODE => (AuthType.user, noParams)
        case OAuthGrantType.REFRESH_TOKEN => (AuthType.user, noParams)
        case OAuthGrantType.CLIENT_CREDENTIALS =>
          (AuthType.client, client_secret match {
            case None =>
              errors = errors.::(InvalidClientSecretException())
              noParams
            case Some(secret) =>
              Map[String, Seq[String]](
                ("client_secret", Seq(secret)),
                ("auth_type", Seq("client"))
              )
          })
        case OAuthGrantType.PASSWORD =>
          (AuthType.user, (username, password, client_secret) match {
            case
              (None, _, _) |
              (_, None, _) |
              (_, _, None) =>
              if (username.isEmpty) errors = errors.::(InvalidUsernameException())
              if (password.isEmpty) errors = errors.::(InvalidPasswordException())
              if (client_secret.isEmpty) errors = errors.::(InvalidClientSecretException())
              noParams
            case (Some(user), Some(pass), Some(cSecret)) =>
              Map[String, Seq[String]](
                ("username", Seq(user)),
                ("password", Seq(pass)),
                ("client_secret", Seq(cSecret)),
                ("auth_type", Seq("user"))
              )
          })
        case OAuthGrantType.IMPLICIT => (AuthType.user, noParams)
        case _ => (AuthType.client, noParams)
      }

      val r = authFlow._2 match {
        case paramsMissing: Map[String, Seq[String]] if paramsMissing == Map.empty[String, Seq[String]] =>
          Future.failed(MultiException(errors))
        case params: Map[String, Seq[String]] =>
          tokenEndpoint.handleRequest(
            new AuthorizationRequest(Map(), m ++ authFlow._2), oauth2Provider
          )(dispatcher)
            .flatMap[OAuth2AccessToken] {
            case Left(err: OAuthError) =>
              throw err
            case Right(grantHandlerResult: GrantHandlerResult[_]) =>
              Future.successful(OAuth2AccessToken(
                grantHandlerResult.accessToken,
                grantHandlerResult.tokenType,
                grantHandlerResult.expiresIn,
                grantHandlerResult.refreshToken,
                grantHandlerResult.scope.map(_.split(" ").toSeq),
                grantHandlerResult.params + ("auth_type" -> authFlow._1.toString)))
          }(dispatcher)
      }

      r
  }

  override def checkToken = ServiceCall { tok: String =>
    accessTokenRepository.find(tok).map {
      case None => false
      case Some(_) => true
    }(dispatcher)
  }

  override def registerClient = ServiceCall { request =>
    val ref = persistentEntityRegistry.refFor[ClientEntity](request.id.s)

    ref.ask(CreateClient(request.id, request.name, request.secret))
  }

  override def showClient(id: String) = ServiceCall { request =>
//    val ref = persistentEntityRegistry.refFor[ClientEntity](id)
//
//    ref.ask(ShowClient)
    val ref = persistentEntityRegistry.refFor[HelloEntity](id)
    ref.ask(Hello(id, None))
  }
}
