package co.tjcelaya.smack.service.auth.impl

import co.tjcelaya.smack.service.auth.api.Client
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

import scala.collection.immutable.Seq

/**
  * Created by tj on 3/25/17.
  */
package object entity {
  object ClientSerializerRegistry extends JsonSerializerRegistry {
    override def serializers: Seq[JsonSerializer[_]] = Seq(
      JsonSerializer[CreateClient],
      JsonSerializer[ClientCreated],
      JsonSerializer[Client]
    )
  }
}
