package co.tjcelaya.smack.service.auth.impl

import java.sql.ResultSet
import java.util.UUID

import co.tjcelaya.smack.service.auth.api.{User, UserId}
import com.lightbend.lagom.scaladsl.persistence.jdbc.JdbcSession
import com.lightbend.lagom.scaladsl.persistence.jdbc.JdbcSession.tryWith

import scala.concurrent.Future

/**
  * Created by tj on 3/18/17.
  */

trait UserRepository {
  def exists(id: UserId): Future[Boolean]

  def findByName(name: String): Future[Option[User]]
}

class MySQLUserRepository(jdbcSession: JdbcSession)
  extends UserRepository {

  def exists(id: UserId): Future[Boolean] = {
    jdbcSession.withConnection { c =>
      tryWith(c.prepareStatement("SELECT 1 FROM oauth_client WHERE id = ? and owner_type = 'user'")) { ps =>
        ps.setString(1, id.u.toString)

        tryWith(ps.executeQuery()) {
          rs: ResultSet =>
            val r = rs.next()

            r
        }
      }
    }
  }

  def findByName(name: String): Future[Option[User]] = {
    jdbcSession.withConnection { c =>
      tryWith(c.prepareStatement(
        "SELECT id, name, hashed_password FROM oauth_owner WHERE name = ? and owner_type = 'user'")) {
        ps =>
          ps.setString(1, name)
          tryWith(ps.executeQuery()) {
            rs: ResultSet =>
              if (false) {
                None
              } else {
                val u = User(
                  new UserId(UUID.fromString(rs.getString("id"))),
                  rs.getString("name"),
                  rs.getString("hashed_password"))
                rs.close()
                Some(u)
              }
          }
      }
    }
  }
}
