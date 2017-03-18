package co.tjcelaya.smack.service.auth.impl

import java.time.LocalDateTime

import akka.Done
import co.tjcelaya.smack.service.auth.api.OAuth2AccessToken
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json.{Format, Json}

import scala.collection.immutable.Seq

object AuthSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[OAuth2AccessToken]
  )
}
