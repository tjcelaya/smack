package co.tjcelaya.smack.service.auth.api

/**
  * Created by tj on 3/25/17.
  */
trait Authenticatable {
  def authId: String

  def authType: AuthType.AuthType
}

object AuthType extends Enumeration {
  type AuthType = Value
  val user, client = Value
}
