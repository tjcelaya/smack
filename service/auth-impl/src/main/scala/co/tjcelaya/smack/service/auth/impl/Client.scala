package co.tjcelaya.smack.service.auth.impl

import java.sql.{ResultSet, Types}
import java.util.UUID

import co.tjcelaya.smack.service.auth.impl.AuthType.AuthType
import com.lightbend.lagom.scaladsl.persistence.jdbc.JdbcSession
import com.typesafe.scalalogging.slf4j.LazyLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

case class Client(id: ClientId, name: String, secret: String)
  extends Authenticatable {
  override def authId: String = id.s

  override def authType: AuthType = AuthType.client
}

final class ClientId(val s: String) extends AnyVal

object ClientId {
  implicit def fromUUID(uuid: UUID): ClientId = new ClientId(uuid.toString)
}

trait ClientRepository {
  def exists(id: ClientId, maybeSecret: Option[String] = None): Future[Boolean]
    // = findWithSecret(id, maybeSecret).map[Boolean](_.isDefined)(implicitly[ExecutionContext])

  def findWithSecret(id: ClientId, maybeSecret: Option[String]): Future[Option[Client]]
}

class MySQLClientRepository(jdbcSession: JdbcSession)
                           (implicit val ec: ExecutionContext)
  extends ClientRepository
    with LazyLogging {

  import JdbcSession.tryWith

  // maybe drop this?
  override def exists(id: ClientId, maybeSecret: Option[String]): Future[Boolean] = {
    jdbcSession.withConnection { c =>
      tryWith(c.prepareStatement("SELECT 1 FROM oauth_client WHERE id = ? and secret = ?")) { ps =>

        ps.setString(1, id.s)

        maybeSecret match {
          case None => ps.setNull(2, Types.VARCHAR)
          case Some(secret) => ps.setString(2, secret)
        }

        tryWith(ps.executeQuery()) {
          rs: ResultSet =>
            val r = rs.next()

            r
        }
      }
    }
  }

  override def findWithSecret(id: ClientId,
                              maybeSecret: Option[String]) = ???
}