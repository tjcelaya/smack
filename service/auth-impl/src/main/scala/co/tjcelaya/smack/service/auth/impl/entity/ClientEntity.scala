package co.tjcelaya.smack.service.auth.impl.entity

/**
  * Created by tj on 3/24/17.
  */

import akka.Done
import co.tjcelaya.smack.service.auth.api.{Client, ClientId}
import com.lightbend.lagom.scaladsl.api.transport
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.typesafe.scalalogging.slf4j.LazyLogging
import play.api.libs.json.{Format, Json}

/**
  * This is an event sourced entity. It has a state, [[Client]], which stores client info
  *
  * Send commands, r/w, either can reply
  *
  * The only thing persisted is the events emitted by the entity
  *
  * Each event will have an event handler registered for it,
  * which applies event to the current state.
  *
  * handlers run when the event is first created and when the entity is revived
  */
class ClientEntity extends PersistentEntity with LazyLogging {

  override type Command = ClientCommand[_]
  override type Event = ClientEvent
  override type State = Client

  val e = new HelloEntity

  /** for when no snapshotted state is found */
  override def initialState: Client = Client(new ClientId(""), "", "")

  /** behaviour is a function of current state to a set of actions */
  override def behavior: Behavior = {

    // empty id -> "nonexistent" entity
    case Client(ClientId(""), _, _) =>
      Actions().onCommand[CreateClient, Done] {
        case (CreateClient(newId, disp_name, sec), ctx, state) =>
          ctx.thenPersist(ClientCreated(newId, disp_name, sec)) { _ =>
            ctx.reply(Done)
          }
      }.onEvent {
        case (ClientCreated(i, n, s), state) =>
          // update the current state to use the supplied data
          Client(i, n, s)
      }.onReadOnlyCommand[ShowClient.type, Client] {
        case (ShowClient, ctx, state) =>
          // TODO: have separate exceptions so entities don't need to know about "transport"
          ctx.commandFailed(transport.NotFound("client"))
      }

    case Client(ClientId(_), _, _) =>
      Actions().onCommand[CreateClient, Done] {
        case (CreateClient(newId, disp_name, sec), ctx, state) =>
          // TODO: have separate exceptions so entities don't need to know about "transport"
          ctx.commandFailed(transport.PolicyViolation("client already exists"))
          ctx.done
      }.onReadOnlyCommand[ShowClient.type, Client] {
        case (ShowClient, ctx, state) =>
          ctx.reply(state)
      }

    // case _ =>
    //   def catchAll: PartialFunction[(ClientCommand[_], CommandContext[_], Client), Persist] = {
    //     case (msg, ctx, state) =>
    //       ctx.invalidCommand(s"Unexpected Command: $msg")
    //       ctx.done
    //   }
    //
    //
    //   Actions()
    //     .onCommand[ClientCommand[Done], Done](catchAll)
    //     .onCommand[ClientCommand[Client], Client](catchAll)
  }
}

// EVENTS //

sealed trait ClientEvent

/** a client was created */
case class ClientCreated(id: ClientId, name: String, secret: String) extends ClientEvent

object ClientCreated {
  /** format needed for storing and loading from database */
  implicit val format: Format[ClientCreated] = Json.format
}

// COMMANDS //

sealed trait ClientCommand[R] extends ReplyType[R]

/** replies with Done once all events emitted by this command are successfully persisted */
case class CreateClient(id: ClientId, display_name: String, secret: String) extends ClientCommand[Done]

object CreateClient {
  /** format needed for sending commands over remoting and other serialization reasons */
  implicit val format: Format[CreateClient] = Json.format
}

case object ShowClient extends ClientCommand[Client] {
  /** format needed for sending commands over remoting and other serialization reasons */
  implicit val format: Format[ShowClient.type] = SingletonFormat.singletonFormat(ShowClient)
}



