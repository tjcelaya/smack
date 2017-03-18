package co.tjcelaya.smack.service.user.impl

import java.util.UUID

import akka.NotUsed
import akka.actor.ActorSystem
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import co.tjcelaya.smack.service.common.LastKnownEnvironment
import co.tjcelaya.smack.service.common.seq.Paginator
import co.tjcelaya.smack.service.user.api
import co.tjcelaya.smack.service.user.api.UserService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import com.lightbend.lagom.scaladsl.persistence.{PersistentEntityRegistry, ReadSide}
import org.slf4j.{Logger, LoggerFactory}
import play.api.Environment

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

class UserServiceImpl(
                       registry: PersistentEntityRegistry,
                       system: ActorSystem,
                       environment: Environment,
                       readSide: ReadSide,
                       cassandraSession: CassandraSession
                     )(
                       implicit ec: ExecutionContext,
                       mat: Materializer
                     ) extends UserService
{

  private final val log: Logger = LoggerFactory.getLogger(classOf[UserServiceImpl])

  LastKnownEnvironment.setEnv(environment)

  private val currentIdsQuery = PersistenceQuery(system).readJournalFor[CassandraReadJournal](CassandraReadJournal.Identifier)

  override def createUser = ServiceCall { createUser =>
    val userId = UUID.randomUUID()
    refFor(userId).ask(CreateUser(createUser.name)).map { _ =>
      api.User(userId, createUser.name)
    }
  }

  override def getUser(userId: UUID) = ServiceCall { _ =>
    refFor(userId).ask(GetUser).map {
      case Some(user) =>
        api.User(userId, user.name)
      case None =>
        throw NotFound(s"User with id $userId")
    }
  }

  override def getUsersFromWriteSide = ServiceCall { _ =>
    // Note this should never make production....
    currentIdsQuery.currentPersistenceIds()
      .filter(_.startsWith("UserEntity|"))
      .mapAsync(4) { id =>
        val entityId = id.split("\\|", 2).last
        registry.refFor[UserEntity](entityId)
          .ask(GetUser)
          .map(_.map(user => api.User(UUID.fromString(entityId), user.name)))
      }.collect {
      case Some(user) => user
    }.runWith(Sink.seq)
  }

  override def getUsersFromReadSide = ServiceCall { _ =>
    val response: Source[api.User, NotUsed] =
      cassandraSession.select("SELECT id, name FROM user")
        .map(row => api.User(row.getUUID("id"), row.getString("name")))

    response.runWith(Sink.seq)
  }

  override def getUsers = ServiceCall { _ =>
    Future.successful(immutable.Seq(api.User(UUID.randomUUID(), "")))
  }

  override def getUsersPage(page: Option[Int] = Some(1), pageSize: Option[Int] = Some(10)) = ServiceCall { _ =>
    val p = Paginator[api.User](
      page.getOrElse(1),
      pageSize.getOrElse(10),
      0,
      immutable.Seq[api.User]())

    for {
      count <- cassandraSession.selectOne(
        """
          |SELECT COUNT(*) FROM user
        """.stripMargin
      ).map {
        case Some(row) => row.getLong("count")
        case None => 0
      }
      items <- if (p.page > p.pageCount) Future.successful(immutable.Seq[api.User]())
      else cassandraSession.selectAll(
        """
          |SELECT id, name FROM user
          |LIMIT ?
        """.stripMargin, Integer.valueOf(p.limit)
      ).map { rows =>
        immutable.Seq(
          rows.drop(p.offset)
            .map(row => api.User(row.getUUID("id"), row.getString("name")))
            : _*
        )
      }
    } yield {
      p.copy(total = count, items = items)
    }
  }

  private def refFor(userId: UUID) = registry.refFor[UserEntity](userId.toString)
}
