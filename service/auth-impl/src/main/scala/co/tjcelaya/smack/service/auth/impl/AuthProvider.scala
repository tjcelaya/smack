package co.tjcelaya.smack.service.auth.impl

import java.util.UUID

import co.tjcelaya.smack.service.common.DateFactory
import com.lightbend.lagom.scaladsl.api.transport.{ExceptionMessage, TransportErrorCode, TransportException}
import com.typesafe.scalalogging.slf4j.LazyLogging
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}
import scalaoauth2.provider._

/**
  * Created by tj on 3/2/17.
  */

sealed trait Authenticatable {
  def authId: String

  def authType: String
}

object UserId {
  implicit def fromUUID(uuid: UUID): UserId = new UserId(uuid)
}

final class UserId(val u: UUID) extends AnyVal

case class User(id: UserId, name: String, hashedPassword: String)
  extends Authenticatable {
  override def authId: String = id.toString

  override def authType: String = "user"
}

object User {
  implicit val idFormat: Format[UserId] = Format(
    Reads.of[UUID].map(new UserId(_)),
    Writes { (uid: UserId) => JsString(uid.u.toString) })
  implicit val format: Format[User] = Json.format[User]
}

object OAuth2ProviderResolver {
  type K = Class[_]
  type V = DataHandler[_]
  private var providers = Map[K, V]()

  def registerProvider(authenticatable: K, handler: V):
  Unit = {
    providers = providers.updated(authenticatable, handler)
  }

  def lookup(authenticatable: K): V = {
    providers(authenticatable)
  }
}

class UserOAuth2Provider(accessTokenRepository: AccessTokenRepository, clientRepository: ClientRepository)
                        (implicit ec: ExecutionContext)
  extends DataHandler[User]
    with LazyLogging {

  def provides: Class[_] = User.getClass

  def validateClient(maybeClientCredential: Option[ClientCredential],
                     request: AuthorizationRequest): Future[Boolean] = {
    request.parseClientCredential match {
      case None => Future.successful(false)
      case Some(maybeValid) =>
        logger.error("some credsz?")
        maybeValid match {
          case Left(invalidClientException) =>
            logger.error(s"invalid client: $invalidClientException")
            throw invalidClientException
          case Right(clientCredential) =>
            clientRepository.exists(clientCredential.clientId)
        }
    }
  }

  "words".toCharArray

  def findUser(maybeClientCredential: Option[ClientCredential],
               request: AuthorizationRequest): Future[Option[User]] = {

    maybeClientCredential match {
      case None =>
        Future.failed(
          new TransportException(
            TransportErrorCode.BadRequest,
            new ExceptionMessage("client_id, client_secret", "client credentials invalid")))
      case Some(ClientCredential(clientId, maybeClientSecret)) =>
        // TODO: do something with request?
        Future.successful(Some(User(UUID.randomUUID(), "bob", "[redacted]")))
    }
  }

  def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = {
    val accessTokenValue =
      s"user_token:${authInfo.user.id} ${authInfo.clientId.getOrElse("client_id_missing:")}"

    def nestedProductToFlatMap(p: Product): Map[String, String] = {
      p.getClass
        .getDeclaredFields
        .map(p.productPrefix + _.getName)
        .zip(p.productIterator.toIterable)
        .foldLeft[Map[String, String]](Map.empty[String, String]) {
        (acc: Map[String, String], pair: (String, Any)) =>
          acc ++ (pair._2 match {
            case ip: Product => nestedProductToFlatMap(ip)
            case scalar => Seq((pair._1, pair._2.toString))
          })
      }
    }

    val token = AccessToken(accessTokenValue, None, None, None, DateFactory.now, nestedProductToFlatMap(authInfo))
    accessTokenRepository.persist(token, authInfo)
  }

  def refreshAccessToken(authInfo: AuthInfo[User], refreshToken: String): Future[AccessToken] = ???

  def getStoredAccessToken(authInfo: AuthInfo[User]): Future[Option[AccessToken]] =
    accessTokenRepository.findByUserId(authInfo.user.id.u)

  def findAccessToken(token: String): Future[Option[AccessToken]] =
    accessTokenRepository.find(token)

  def findAuthInfoByCode(code: String): Future[Option[AuthInfo[User]]] = ???

  def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[User]]] = ???

  def findAuthInfoByAccessToken(accessToken: AccessToken): Future[Option[AuthInfo[User]]] = ???

  def deleteAuthCode(code: String): Future[Unit] = ???
}
