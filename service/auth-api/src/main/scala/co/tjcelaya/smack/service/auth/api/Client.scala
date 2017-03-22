package co.tjcelaya.smack.service.auth.api

import java.util.UUID

import play.api.libs.json.{Format, Json}

/**
  * Created by tj on 3/25/17.
  */

case class Client(id: ClientId, name: String, secret: String)
  extends Authenticatable {
  override def authId: String = id.s

  override def authType: AuthType.AuthType = AuthType.client
}

object Client {
  implicit val format: Format[Client] = Json.format
}

final case class ClientId(s: String) extends AnyVal

object ClientId {
  // implicit def fromUUID(uuid: UUID): ClientId = new ClientId(uuid.toString)
  implicit val format: Format[ClientId] = Json.format
}
