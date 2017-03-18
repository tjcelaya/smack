package co.tjcelaya.smack.service.user.api

import java.util.UUID

import co.tjcelaya.smack.service.common.JsonApiTypeable
import play.api.libs.json.{Format, Json}

/**
  * Created by tj on 2/21/17.
  */

case class User(id: UUID, name: String) extends JsonApiTypeable {
  def getType: String = "user"
  override def getId: String = id.toString
}

object User {
  implicit val format: Format[User] = Json.format
}

case class CreateUser(name: String)

object CreateUser {
  implicit val format: Format[CreateUser] = Json.format
}
