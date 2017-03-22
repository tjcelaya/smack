package co.tjcelaya.smack.service.auth.impl.entity

/**
  * Created by tj on 3/24/17.
  */

import akka.Done
import co.tjcelaya.smack.service.auth.api.{Client, ClientId}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
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
class ClientEntity extends PersistentEntity {

  override type Command = ClientCommand[_]
  override type Event = ClientEvent
  override type State = Client

  val e = new HelloEntity

  /** for when no snapshotted state is found */
  override def initialState: Client = Client(new ClientId(""), "", "")

  /** behaviour is a function of current state to a set of actions */
  override def behavior: Behavior = {
    case Client(ClientId(""), _, _) =>
      Actions().onCommand[CreateClient, Done] {
        // Command handler for the CreateClient command
        case (CreateClient(newId, disp_name, sec), ctx, state) =>
          // In response to this command, we want to first persist it as a
          // ClientCreated event
          ctx.thenPersist(
            ClientCreated(newId, disp_name, sec)
          ) { _ =>
            // Then once the event is successfully persisted, we respond with done.
            ctx.reply(Done)
          }
      } onEvent {
        case (ClientCreated(i, n, s), state) =>
          // We simply update the current state to use the supplied data
          Client(i, n, s)
      }
    case Client(ClientId(_), _, _) =>
      Actions().onReadOnlyCommand[ShowClient.type, Client] {
        // Command handler for the ShowClient command
        case (ShowClient, ctx, state) =>
          // Reply with a message built from the current message, and the name of
          // the person we're meant to say hello to.
          ctx.reply(state)

      }
    case _ =>
      def catchAll:PartialFunction[(ClientCommand[_], CommandContext[_], Client), Persist] = {
        case (msg, ctx, state) =>
          ctx.invalidCommand(s"Unexpected Command: $msg")
          ctx.done
      }
      println("unexpected?")
      Actions()
        .onCommand[ClientCommand[Done], Done](catchAll)
        .onCommand[ClientCommand[Client], Client](catchAll)
  }
}

// EVENTS //
//        //
//        //

sealed trait ClientEvent

/** a client was created */
case class ClientCreated(id: ClientId, name: String, secret: String) extends ClientEvent

object ClientCreated {
  /** format needed for storing and loading from database */
  implicit val format: Format[ClientCreated] = Json.format
}

// COMMANDS //
//        //
//        //


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



