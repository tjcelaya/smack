package co.tjcelaya.smack.service.auth.impl

import java.util.UUID

import co.tjcelaya.smack.service.auth.impl.AuthType.AuthType
import play.api.libs.json._

import scala.language.implicitConversions

object User {
  implicit val idFormat: Format[UserId] = Format(
    Reads.of[UUID].map(new UserId(_)),
    Writes { (uid: UserId) => JsString(uid.u.toString) })
  implicit val format: Format[User] = Json.format[User]
}

case class User(id: UserId, name: String, hashedPassword: String)
  extends Authenticatable {
  override def authId: String = id.toString

  override def authType: AuthType = AuthType.user
}

final class UserId(val u: UUID) extends AnyVal
