package co.tjcelaya.smack.service.common

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.Descriptor.{NamedCallId, PathCallId, RestCallId}
import com.lightbend.lagom.scaladsl.api.{Descriptor, ServiceCall}
import play.api.libs.json.{JsArray, JsObject, JsString}

import scala.concurrent.Future

/**
  * Created by tj on 2/21/17.
  */
object OptionsServiceCallGenerator {
  def apply(descriptor: Descriptor): ServiceCall[NotUsed, JsArray] = {
    ServiceCall { _ =>
      Future.successful(
        JsArray(
          descriptor.calls.map(call => {
            call.callId match {
              case restC: RestCallId =>
                JsObject(
                  Seq(
                    "method" -> JsString(restC.method.name),
                    "path" -> JsString(restC.pathPattern)
                  )
                )
              case _: NamedCallId |
                   _: PathCallId => JsString("nyi")
            }
          })
        )
      )
    }
  }
}
