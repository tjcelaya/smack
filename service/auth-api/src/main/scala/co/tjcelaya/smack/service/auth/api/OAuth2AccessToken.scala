package co.tjcelaya.smack.service.auth.api

import play.api.libs.json.{Format, Json}

/**
  * Created by tj on 3/25/17.
  */

case class OAuth2AccessToken(access_token: String,
                             token_type: String,
                             expires_in: Option[Long],
                             refresh_token: Option[String],
                             scope: Option[Seq[String]],
                             params: Map[String, String])


object OAuth2AccessToken {
  implicit val formatToken: Format[OAuth2AccessToken] = Json.format
}
