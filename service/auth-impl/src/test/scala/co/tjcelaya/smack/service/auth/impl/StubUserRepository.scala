package co.tjcelaya.smack.service.auth.impl

import java.util.UUID

import co.tjcelaya.smack.service.auth.api.{User, UserId}

import scala.concurrent.Future
import scala.util.Random

/**
  * Created by tj on 3/18/17.
  */
class StubUserRepository extends UserRepository {
  var users = List[User]()

  override def exists(id: UserId) = Future.successful(users.exists(_.id == id))

  override def findByName(name: String) = Future.successful(users.find(_.name == name))

  def seed: (String, String) = {
    val un = Random.alphanumeric take 5 mkString ""
    val pw = Random.alphanumeric take 10 mkString ""
    users = User(new UserId(UUID.randomUUID), un, pw) :: users
    (un, pw)
  }
}