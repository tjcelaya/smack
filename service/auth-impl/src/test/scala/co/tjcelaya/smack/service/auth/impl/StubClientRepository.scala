package co.tjcelaya.smack.service.auth.impl

import java.util.UUID

import co.tjcelaya.smack.service.auth.api.{Client, ClientId}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

/**
  * Created by tj on 3/18/17.
  */
class StubClientRepository(implicit ec: ExecutionContext) extends ClientRepository {
  var clients = List[Client]()

  override def exists(id: ClientId,
                      maybeSecret: Option[String]) =
    findWithSecret(id, maybeSecret).map[Boolean](_.isDefined)

  override def findWithSecret(id: ClientId,
                              maybeSecret: Option[String]): Future[Option[Client]] = {
    require(maybeSecret.isDefined)
    Future.successful(clients.find(c => c.id == id && c.secret == maybeSecret.get))
  }

  def seed: (String, String) = {
    val cid = UUID.randomUUID.toString
    val un = Random.alphanumeric take 5 mkString ""
    val csecret = Random.alphanumeric take 10 mkString ""
    clients = Client(new ClientId(cid), un, csecret) :: clients
    (cid, csecret)
  }
}
