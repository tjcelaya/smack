package co.tjcelaya.smack.service.user.impl

import akka.Done
import com.datastax.driver.core.{BoundStatement, PreparedStatement}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, EventStreamElement, ReadSideProcessor}
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future, Promise}

/**
  * Created by tj on 2/22/17.
  */
class UserEventProcessor(session: CassandraSession, readSide: CassandraReadSide)
                        (implicit ec: ExecutionContext)
  extends ReadSideProcessor[UserEvent] {
  private final val log: Logger = LoggerFactory.getLogger(classOf[UserEventProcessor])

  private val writeUserPromise = Promise[PreparedStatement] // initialized in prepare
  private def writeUser: Future[PreparedStatement] = writeUserPromise.future

  private def prepareWriteUser(): Future[Done] = {
    val f = session.prepare("INSERT INTO user (id, name) VALUES (:id, :name)")
    writeUserPromise.completeWith(f)
    f.map(_ => Done)
  }

  private def processUserCreated(eventElement: EventStreamElement[UserCreated]): Future[List[BoundStatement]] = {
    writeUser.map { ps =>
      log.warn(s"processing UserCreated entityId: ${eventElement.entityId}, event: ${eventElement.event}\n$eventElement")
      val bindWriteTitle = ps.bind()
      bindWriteTitle.setUUID("id", eventElement.event.id)
      bindWriteTitle.setString("name", eventElement.event.name)
      List(bindWriteTitle)
    }
  }

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[UserEvent] = {
    val builder = readSide.builder[UserEvent]("useroffset")
    builder.setGlobalPrepare(() =>
      session.executeCreateTable(
        """
          |CREATE TABLE IF NOT EXISTS user (
          |id UUID, name VARCHAR, PRIMARY KEY (id))
        """.stripMargin))

    builder.setPrepare(t => prepareWriteUser())

    builder.setEventHandler[UserCreated](processUserCreated)

    session.underlying().map(s => {
      val msg = "cassandra port is: " + s.getCluster.getConfiguration.getProtocolOptions.getPort
      log.warn(msg)
    })

    builder.build()
  }

  override def aggregateTags: Set[AggregateEventTag[UserEvent]] = Set(UserEvent.UserEventTag)
}
