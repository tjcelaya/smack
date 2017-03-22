package co.tjcelaya.smack.service.auth.api

import com.lightbend.lagom.scaladsl.api.transport.{TransportErrorCode, TransportException}

/**
  * Created by tj on 3/25/17.
  */
package object exceptions {
  case class BadGrantTypeException(grantType: String)
    extends TransportException(TransportErrorCode.BadRequest, s"grant_type provided is invalid or unsupported: $grantType")

  case class InvalidClientSecretException()
    extends TransportException(TransportErrorCode.BadRequest, s"client_secret must be provided")

  case class InvalidUsernameException()
    extends TransportException(TransportErrorCode.BadRequest, s"username must be provided")

  case class InvalidPasswordException()
    extends TransportException(TransportErrorCode.BadRequest, s"Password must be provided")
}
