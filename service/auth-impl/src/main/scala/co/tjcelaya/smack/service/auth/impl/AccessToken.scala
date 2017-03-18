package co.tjcelaya.smack.service.auth.impl

import java.sql.{Connection, Statement}
import java.util.{Date, UUID}

import co.tjcelaya.smack.service.common.DateFactory
import com.typesafe.scalalogging.slf4j.LazyLogging
import play.api.libs.functional.syntax._
import play.api.libs.json._
import scredis.Client
import scredis.serialization.{Reader, Writer}

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

  def findByUserId(id: UUID): Future[Option[AccessToken]]

  def revoke(token: String): Future[Unit]
}

class RedisAccessTokenRepository(redisClient: Client)
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

  def findByUserId(id: UUID): Future[Option[AccessToken]] =
    redisClient.get[AccessToken](s"token:user_id:$id")(AccessTokenRedisReader)

  def revoke(token: String): Future[Unit] =
    redisClient.del("token:" + token).map[Unit](_ -> ())
}

class MySQLAccessTokenRepository()
                                (implicit ec: ExecutionContext)
  extends AccessTokenRepository
    with LazyLogging {

  import slick.jdbc.MySQLProfile.api._

  private val db = Database.forConfig("auth-mysql")

  override def persist(accessToken: AccessToken,
                       authInfo: AuthInfo[_ <: Authenticatable]): Future[AccessToken] = {
//      val inserts = for {
//      sessionI <-
//        sqlu"""
//        INSERT INTO oauth_session (client_id, owner_type, owner_id, client_redirect_uri)
//        VALUES (${authInfo.clientId}, ${authInfo.user.authType}, ${authInfo.user.authId}, ${authInfo.redirectUri})"""
//      scopeI <-
//        sqlu"""
//        INSERT INTO oauth_access_token_scope (access_token_id, scope_id)
//        VALUES (${accessToken.token}, ${accessToken.scope.get.mkString(" ")})
//        """
//
//      """
//        INSERT INTO oauth_refresh_token (id, access_token_id, expire_time)
//        VALUES (${accessToken.refreshToken.get}, ${accessToken.token}, ${accessToken.expiresIn})
//        """
//      """
//        INSERT INTO oauth_access_token (id, session_id, expire_time)
//        VALUES (${accessToken.token}, $insertedSessionId, ${accessToken.expiresIn})
//        """
//    } yield { uI + tI }

    //   LEFT JOIN oauth_refresh_token ort
    // ON oat.id = ort.access_token_id
    //   LEFT JOIN oauth_access_token_param_map oatparams
    // ON oat.id = oatparams.access_token_id
    // AND oat.session_id = oatparams.session_id


//    val maybeInserted: Future[Int] = db.run(inserts.transactionally)
//    maybeInserted.map[AccessToken] { affected =>
//      logger.error(s"created token $affected")
      Future.successful(accessToken)
//    }
  }

  override def find(token: String): Future[Option[AccessToken]] = {
    val tokenSelect = sql"""SELECT id FROM oauth_access_token""".as[(String)]
    //        SELECT
    //          oat.id token
    //          -- ort.id refreshToken,
    //          -- GROUP_CONCAT(os.id) scope,
    //          -- oat.expire_time lifeSeconds,
    //          -- oat.created_at,
    //          -- oatparams.params
    //        FROM oauth_access_token oat
    //        LEFT JOIN oauth_access_token_scope oats
    //          ON oat.id = oats.access_token_id
    //        LEFT JOIN oauth_scope os
    //          ON oats.scope_id = os.id
    //        LEFT JOIN oauth_refresh_token ort
    //          ON oat.id = ort.access_token_id
    //        LEFT JOIN oauth_access_token_param_map oatparams
    //          ON oat.id = oatparams.access_token_id
    //          AND oat.session_id = oatparams.session_id
    //        WHERE
    //          oat.id = $token

    // """.as[(String, Option[String], Option[String], Option[Long], Date, Map[String, String])]

    val maybeRow: Future[Seq[String]] = db.run(tokenSelect)
    maybeRow.map[Option[AccessToken]] {
      case empty: Seq[_] if empty.isEmpty => None
      case found: Seq[(String)] => Some(AccessToken("t", None, None, None, DateFactory.now))
    }
  }

  override def findByUserId(id: UUID): Future[Option[AccessToken]] = {
    val idStr = id.toString
    logger.error(s"user id $idStr")
    val tokenSelect =
      sql"""SELECT
          oat.id token
        FROM oauth_owner oo
        JOIN oauth_session osess
          ON oo.id = osess.owner_id
          AND oo.owner_type = osess.owner_type
        JOIN oauth_access_token oat
          ON oat.session_id = osess.id
        LEFT JOIN oauth_access_token_scope oats
          ON oat.id = oats.access_token_id
        LEFT JOIN oauth_scope osc
          ON oats.scope_id = osc.id
        LEFT JOIN oauth_refresh_token ort
          ON oat.id = ort.access_token_id
        LEFT JOIN oauth_access_token_param_map oatparams
          ON oat.id = oatparams.access_token_id
          AND oat.session_id = oatparams.session_id
        WHERE
          oo.id = $idStr
        """.as[(String)]

    db.run(tokenSelect)
      .map[Option[AccessToken]] {
      case empty: Seq[_] if empty.isEmpty =>
        None
      case expectedUnique: Seq[_] if 1 < expectedUnique.size =>
        logger.error(
          s"unexpected result size in findByUserId from id: $idStr , size: ${expectedUnique.size}")
        None
      case found: Seq[String] if found.size == 1 =>
        Some(AccessToken(found.head, None, None, None, DateFactory.now))
    }
  }

  override def revoke(token: String): Future[Unit] = ???
}

trait AuthorizationCodeRepository {
  def create(authorizationCode: AuthorizationCode): Future[AuthorizationCode]

  def findByUserId(id: UUID): Future[AuthorizationCode]

  def delete(): Future[Unit]
}

class ClientRepository()(implicit ec: ExecutionContext) extends LazyLogging {

  import slick.jdbc.MySQLProfile.api._

  private val db = Database.forConfig("auth-mysql")

  override def finalize(): Unit = {
    db.close()
    super.finalize()
  }

  def exists(id: String): Future[Boolean] = {
    db.run(sql"""SELECT 1 FROM oauth_client WHERE id = $id""".as[(Int)])
      .map[Boolean] {
      case cs: Seq[Int] if 0 < cs.size => true
      case _ => false
    }
  }
}