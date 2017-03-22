package co.tjcelaya.smack.service.auth.impl

import java.sql.{PreparedStatement, Statement}
import java.util.{Date, UUID}

import co.tjcelaya.smack.service.auth.api.{Authenticatable, Client, User}
import co.tjcelaya.smack.service.common.DateFactory
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.jdbc.JdbcSession
import com.typesafe.scalalogging.slf4j.LazyLogging
import play.api.libs.functional.syntax._
import play.api.libs.json._
import scredis.serialization.{Reader, Writer}
import scredis.{Client => RedisClient}

import scala.concurrent.{ExecutionContext, Future}
import scalaoauth2.provider.{AccessToken, AuthInfo, AuthorizationCode}

/**
  * Created by tj on 3/1/17.
  */
object AccessTokenFormatter {
  implicit val accessTokenJsonFormatter: Format[AccessToken] = (
    (__ \ "token").format[String] and
      (__ \ "refresh_token").formatNullable[String] and
      (__ \ "scope").formatNullable[String] and
      (__ \ "expires_in").formatNullable[Long] and
      (__ \ "created_at").format[Date] and
      (__ \ "params").format[Map[String, String]]
    ) (AccessToken.apply, unlift(AccessToken.unapply))

  implicit object AccessTokenRedisWriter extends Writer[AccessToken] {
    override def writeImpl(accessToken: AccessToken): Array[Byte] = {
      Json.toJson(accessToken).toString.getBytes
    }
  }

  implicit object AccessTokenRedisReader extends Reader[AccessToken] {
    override def readImpl(bytes: Array[Byte]): AccessToken = {
      Json.parse(new String(bytes)).as[AccessToken]
    }
  }

}

trait AccessTokenRepository {
  def persist(accessToken: AccessToken, authInfo: AuthInfo[_ <: Authenticatable]): Future[AccessToken]

  def find(token: String): Future[Option[AccessToken]]

  def findByAuthenticatable(authenticatable: Authenticatable): Future[Option[AccessToken]]

  def revoke(token: String): Future[Unit]
}

class RedisAccessTokenRepository(redisClient: RedisClient)
                                (implicit ec: ExecutionContext)
  extends
    AccessTokenRepository {

  import AccessTokenFormatter._

  def persist(accessToken: AccessToken, authInfo: AuthInfo[_ <: Authenticatable]): Future[AccessToken] =
    redisClient.withTransaction { t =>
      t.set[AccessToken]("token:" + accessToken.token, accessToken)(AccessTokenRedisWriter)
      t.set[AccessToken](s"token:user_id:${authInfo.user.authId}", accessToken)(AccessTokenRedisWriter)
      Future.successful(accessToken)
    }

  def find(token: String): Future[Option[AccessToken]] =
    redisClient.get[AccessToken](s"token:$token")(AccessTokenRedisReader)

  def findByAuthenticatable(authenticatable: Authenticatable): Future[Option[AccessToken]] = {
    val authType = authenticatable match {
      case _: User => "user"
      case _: Client => "client"
    }

    redisClient.get[AccessToken](s"token:${authType}_id:${authenticatable.authId}")(AccessTokenRedisReader)
  }

  def revoke(token: String): Future[Unit] =
    redisClient.del("token:" + token).map[Unit](_ => ())
}

class MySQLAccessTokenRepository(persistentEntityRegistry: PersistentEntityRegistry)
  extends AccessTokenRepository
    with LazyLogging {

  override def persist(accessToken: AccessToken,
                       authInfo: AuthInfo[_ <: Authenticatable]): Future[AccessToken] = {
    // persistentEntityRegistry.refFor[AccessTokenEntity](accessToken.token)


    Future.successful(accessToken)
  }

  override def find(token: String): Future[Option[AccessToken]] = {
    Future.successful(None)
  }

  override def findByAuthenticatable(authenticatable: Authenticatable): Future[Option[AccessToken]] = {
    val idStr = authenticatable.authId.toString
    logger.error(s"user id $idStr")
    Future.successful(None)
  }

  override def revoke(token: String): Future[Unit] =
    ???
}

trait AuthorizationCodeRepository {
  def create(authorizationCode: AuthorizationCode): Future[AuthorizationCode]

  def findByUserId(id: UUID): Future[AuthorizationCode]

  def delete(): Future[Unit]
}
