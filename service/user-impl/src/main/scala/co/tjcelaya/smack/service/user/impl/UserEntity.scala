package co.tjcelaya.smack.service.user.impl

import java.util.UUID

import akka.Done
import co.tjcelaya.smack.service.common.seq.Paginator
import co.tjcelaya.smack.service.user.api
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.data.validation.ValidationError
import play.api.libs.json.{Format, Json, Reads, Writes, _}

class UserEntity extends PersistentEntity {
  override type Command = UserCommand
  override type Event = UserEvent
  override type State = Option[User]

  override def initialState = None

  override def behavior: Behavior = {
    case Some(user) =>
      Actions().onReadOnlyCommand[GetUser.type, Option[User]] {
        case (GetUser, ctx, state) => ctx.reply(state)
      }.onReadOnlyCommand[CreateUser, Done] {
        case (CreateUser(name), ctx, state) => ctx.invalidCommand("User already exists")
      }
    case None =>
      Actions().onReadOnlyCommand[GetUser.type, Option[User]] {
        case (GetUser, ctx, state) => ctx.reply(state)
      }.onCommand[CreateUser, Done] {
        case (CreateUser(name), ctx, state) =>

          ctx.thenPersist(UserCreated(UUID.fromString(this.entityId), name))(_ => ctx.reply(Done))
      }.onEvent {
        case (UserCreated(id, name), state) => Some(User(name))
      }
  }
}

case class User(name: String)

object User {
  implicit val format: Format[User] = Json.format
}

object UserEvent {
  val UserEventTag: AggregateEventTag[UserEvent] = AggregateEventTag[UserEvent]
}

sealed trait UserEvent extends AggregateEvent[UserEvent] {
  override def aggregateTag: AggregateEventTag[UserEvent] =
    UserEvent.UserEventTag
}

case class UserCreated(id: UUID, name: String) extends UserEvent

object UserCreated {
  implicit val format: Format[UserCreated] = Json.format
}

sealed trait UserCommand

case class CreateUser(name: String) extends UserCommand with ReplyType[Done]

object CreateUser {
  implicit val format: Format[CreateUser] = Json.format
}

case object GetUser extends UserCommand with ReplyType[Option[User]] {

  def singletonReads[O](singleton: O): Reads[O] = {
    (__ \ "value").read[String].collect(
      ValidationError(s"Expected a JSON object with a single field with key 'value' and value '${singleton.getClass.getSimpleName}'")
    ) {
      case s if s == singleton.getClass.getSimpleName => singleton
    }
  }

  def singletonWrites[O]: Writes[O] = Writes { singleton =>
    Json.obj("value" -> singleton.getClass.getSimpleName)
  }

  def singletonFormat[O](singleton: O): Format[O] = {
    Format(singletonReads(singleton), singletonWrites)
  }

  implicit val format: Format[GetUser.type] = singletonFormat(GetUser)
}

object UserSerializerRegistry extends JsonSerializerRegistry {
  override def serializers = List(
    JsonSerializer[User],
    JsonSerializer[UserCreated],
    JsonSerializer[CreateUser],
    JsonSerializer[GetUser.type],
    JsonSerializer[Paginator[api.User]]
  )
}