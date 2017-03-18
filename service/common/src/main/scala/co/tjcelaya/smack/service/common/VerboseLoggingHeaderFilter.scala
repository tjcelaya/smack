package co.tjcelaya.smack.service.common

import java.util

import com.google.common.base.Splitter
import com.lightbend.lagom.scaladsl.api.security.ServicePrincipal
import com.lightbend.lagom.scaladsl.api.transport.{HeaderFilter, Method, RequestHeader, ResponseHeader}
import org.slf4j.{Logger, LoggerFactory}
import play.api.http.HeaderNames
import play.api.mvc.Action

/**
  * Created by tj on 2/21/17.
  */
object VerboseLoggingHeaderFilter {
  private final val log: Logger = LoggerFactory.getLogger(classOf[VerboseLoggingHeaderFilter])
}

class VerboseLoggingHeaderFilter(name: String = "generic-request-logger")
  extends HeaderFilter {

  import VerboseLoggingHeaderFilter._

  private def logStep(request: RequestHeader, step: String): Unit = {
    log.info(s"$name $step ${request.method} ${request.uri}")
  }

  def transformClientRequest(request: RequestHeader): RequestHeader = {
    logStep(request, "LAGOM-CLIENT-REQ")
    request
  }

  def transformServerRequest(request: RequestHeader): RequestHeader = {
    logStep(request, "LAGOM-SERVER-REQ")
    request
  }

  def transformServerResponse(response: ResponseHeader, request: RequestHeader): ResponseHeader = {

    logStep(request, "LAGOM-SERVER-RES")
    response
  }

  def transformClientResponse(response: ResponseHeader, request: RequestHeader): ResponseHeader = {

    logStep(request, "LAGOM-CLIENT-RES")
    response
  }
}
