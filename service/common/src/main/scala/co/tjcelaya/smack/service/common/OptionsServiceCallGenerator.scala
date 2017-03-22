package co.tjcelaya.smack.service.common

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.Descriptor.RestCallId
import com.lightbend.lagom.scaladsl.api.{Descriptor, ServiceCall}
import play.api.libs.json.{JsArray, JsObject, JsString}

import scala.concurrent.Future

/**
  * Created by tj on 2/21/17.
  */
object OptionsServiceCallGenerator {
  def apply(descriptor: Descriptor): ServiceCall[NotUsed, JsArray] = {
    ServiceCall { _ =>
      val restCallIds = descriptor.calls
        .map(_.callId)
        .filter(_.isInstanceOf[RestCallId])
        .map(_.asInstanceOf[RestCallId])
        .map((rCI: RestCallId) =>
          JsObject(
            Seq(
              "method" -> JsString(rCI.method.name),
              "path" -> JsString(rCI.pathPattern)
            )
          )
        )

      Future.successful(JsArray(restCallIds))
    }
  }
}
