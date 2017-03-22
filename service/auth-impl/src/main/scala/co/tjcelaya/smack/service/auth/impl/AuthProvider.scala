package co.tjcelaya.smack.service.auth.impl

import co.tjcelaya.smack.service.auth.api.{Authenticatable, ClientId, User}
import co.tjcelaya.smack.service.common.DateFactory
import com.lightbend.lagom.scaladsl.api.transport.{ExceptionMessage, TransportErrorCode, TransportException}
import com.typesafe.scalalogging.slf4j.LazyLogging

import scala.concurrent.{ExecutionContext, Future}
import scalaoauth2.provider._

/**
  * Created by tj on 3/2/17.
  */


class OAuth2Provider(accessTokenRepository: AccessTokenRepository,
                     clientRepository: ClientRepository,
                     userRepository: UserRepository)
                    (implicit ec: ExecutionContext)
  extends DataHandler[Authenticatable]
    with LazyLogging {

  def provides: Class[_] = User.getClass

  def validateClient(maybeClientCredential: Option[ClientCredential],
                     request: AuthorizationRequest): Future[Boolean] = {
    request.parseClientCredential match {
      case None => Future.successful(false)
      case Some(Left(invalidClientException)) =>
        logger.error(s"invalid client: $invalidClientException")
        throw invalidClientException
      case Some(Right(clientCredential)) =>
        clientRepository.exists(new ClientId(clientCredential.clientId), clientCredential.clientSecret)
    }
  }

  def findUser(maybeClientCredential: Option[ClientCredential],
               request: AuthorizationRequest): Future[Option[Authenticatable]] = {

    maybeClientCredential match {
      case None =>
        Future.failed(
          new TransportException(
            TransportErrorCode.BadRequest,
            new ExceptionMessage("client_id, client_secret", "client credentials invalid")))
      case Some(ClientCredential(clientId, maybeClientSecret)) =>
        clientRepository.findWithSecret(new ClientId(clientId), maybeClientSecret)
    }
  }

  def createAccessToken(authInfo: AuthInfo[Authenticatable]): Future[AccessToken] = {
    val accessTokenValue =
      s"user_token:${
        authInfo.user.authId
      } ${
        authInfo.clientId.getOrElse("client_id_missing:")
      }"

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

  def refreshAccessToken(authInfo: AuthInfo[Authenticatable], refreshToken: String): Future[AccessToken] = ???

  def getStoredAccessToken(authInfo: AuthInfo[Authenticatable]): Future[Option[AccessToken]] =
    accessTokenRepository.findByAuthenticatable(authInfo.user)

  def findAccessToken(token: String): Future[Option[AccessToken]] =
    accessTokenRepository.find(token)

  def findAuthInfoByCode(code: String): Future[Option[AuthInfo[Authenticatable]]] = ???

  def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[Authenticatable]]] = ???

  def findAuthInfoByAccessToken(accessToken: AccessToken): Future[Option[AuthInfo[Authenticatable]]] = ???

  def deleteAuthCode(code: String): Future[Unit] = ???
}
