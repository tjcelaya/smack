package co.tjcelaya.smack.service.common

import java.io.{CharArrayWriter, PrintWriter}

import akka.util.ByteString
import com.lightbend.lagom.scaladsl.api.deser.{ExceptionSerializer, RawExceptionMessage}
import com.lightbend.lagom.scaladsl.api.transport._
import com.typesafe.scalalogging.Logger
import com.typesafe.scalalogging.slf4j.LazyLogging
import play.api.libs.json.{JsError, JsResultException, JsSuccess, Json}
import play.api.{Environment, Mode}

import scala.collection.immutable.Seq
import scala.util.control.NonFatal

/**
  * Created by tj on 2/22/17.
  */
/**
  * A modified version of the default exception serializer.
  *
  * Serializes exception messages to JSON.
  *
  * This serializer is capable of converting Lagom built-in exceptions to and from JSON. Custom sub classes of
  * TransportException can also be deserialized by extending this class and overriding [[fromCodeAndMessage()]].
  */
class FriendlyExceptionSerializer(environment: Environment)
  extends ExceptionSerializer
  with LazyLogging {

  override def serialize(exception: Throwable, accept: Seq[MessageProtocol]): RawExceptionMessage = {
    val (errorCode, message) = exception match {
      case me: MultiException =>
        //  (TransportErrorCode.BadRequest, new ExceptionMessage("multiple errors occured", me.exceptionMessage))
        (TransportErrorCode.BadRequest, me.exceptionMessage)
      case de: DeserializationException if de.getCause.isInstanceOf[JsResultException] =>
        val errors: String = de.getCause.asInstanceOf[JsResultException].errors.map(err => {
          s"path: ${err._1}, " + err._2.map(_.message).mkString(",")
        }).mkString("\n")
        (TransportErrorCode.BadRequest, new ExceptionMessage("Exception", errors))
      case e if environment.mode == Mode.Prod =>
        (TransportErrorCode.InternalServerError, new ExceptionMessage("Exception", "internal"))
      case te: TransportException =>
        (te.errorCode, te.exceptionMessage)
      case e =>
        // Ok to give out exception information in dev and test
        val writer = new CharArrayWriter
        e.printStackTrace(new PrintWriter(writer))
        val detail = writer.toString
        (TransportErrorCode.InternalServerError, new ExceptionMessage(s"${exception.getClass.getName}: ${exception.getMessage}", detail))
    }

    val messageBytes = ByteString.fromString(Json.stringify(Json.obj(
      "name" -> message.name,
      "detail" -> message.detail
    )))

    val rawEx =
      RawExceptionMessage(
        errorCode,
        MessageProtocol(Some("application/json"), None, None),
        messageBytes)


    logger.error("friendly exception found something for ya", rawEx)

    rawEx
  }

  override def deserialize(message: RawExceptionMessage): Throwable = {
    val messageJson = try {
      Json.parse(message.message.iterator.asInputStream)
    } catch {
      case NonFatal(e) =>
        Json.obj()
    }

    val jsonParseResult = for {
      name <- (messageJson \ "name").validate[String]
      detail <- (messageJson \ "detail").validate[String]
    } yield new ExceptionMessage(name, detail)

    val exceptionMessage = jsonParseResult match {
      case JsSuccess(m, _) => m
      case JsError(_) => new ExceptionMessage("UndeserializableException", message.message.utf8String)
    }

    fromCodeAndMessage(message.errorCode, exceptionMessage)
  }

  /**
    * Override this if you wish to deserialize your own custom Exceptions.
    *
    * The default implementation delegates to [[TransportException.fromCodeAndMessage()]], which will return a best match
    * Lagom built-in exception.
    *
    * @param transportErrorCode The transport error code.
    * @param exceptionMessage   The exception message.
    * @return The exception.
    */
  protected def fromCodeAndMessage(transportErrorCode: TransportErrorCode,
                                   exceptionMessage: ExceptionMessage): Throwable = {
    TransportException.fromCodeAndMessage(transportErrorCode, exceptionMessage)
  }
}


case class MultiException(exs: Seq[TransportException])
  extends TransportException(TransportErrorCode.BadRequest, exs.mkString(", "))
