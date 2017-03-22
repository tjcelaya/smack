package co.tjcelaya.smack.service.auth.impl.entity

import play.api.data.validation.ValidationError
import play.api.libs.json._

/**
  * Created by tj on 3/25/17.
  */
object SingletonFormat {
  def singletonReads[O](singleton: O): Reads[O] = {
    (__ \ "value").read[String].collect(
      ValidationError(
        s"Expected a JSON object with a single field with key 'value' and value '${singleton.getClass.getSimpleName}'")
    ) {
      case s if s == singleton.getClass.getSimpleName => singleton
    }
  }

  def singletonWrites[O]: Writes[O] = Writes { singleton =>
    Json.obj("value" -> singleton.getClass.getSimpleName)
  }

  def singletonFormat[O](singleton: O): Format[O] = {
    Format(singletonReads(singleton), singletonWrites)
  }
}
