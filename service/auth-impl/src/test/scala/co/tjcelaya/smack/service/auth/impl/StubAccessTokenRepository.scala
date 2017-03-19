package co.tjcelaya.smack.service.auth.impl

import scala.concurrent.Future
import scalaoauth2.provider.{AccessToken, AuthInfo}

/**
  * Created by tj on 3/18/17.
  */
class StubAccessTokenRepository extends AccessTokenRepository {
  var toks: List[AccessToken] = List()

  override def find(token: String): Future[Option[AccessToken]] =
    Future.successful(toks.find(_.token == token))

  override def findByAuthenticatable(authenticatable: Authenticatable): Future[Option[AccessToken]] = {
    // find does -> toks.filter(_.params.get("user_id") == id.toString).headOption
    Future.successful(toks.find(_.params.get("user.user_id") == authenticatable.authId.toString))
  }

  override def persist(accessToken: AccessToken,
                       authInfo: AuthInfo[_ <: Authenticatable]): Future[AccessToken] = {
    toks = accessToken :: toks
    Future.successful(accessToken)
  }

  override def revoke(token: String): Future[Unit] = {
    toks = toks.filter(_.token != token)
    Future.successful(())
  }
}
