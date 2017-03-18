package co.tjcelaya.smack.service.common.seq

import play.api.libs.json._
import play.api.libs.functional.syntax._
import org.zalando.jsonapi.json.playjson.PlayJsonJsonapiSupport._
import co.tjcelaya.smack.service.common.JsonApiTypeable
import org.zalando.jsonapi.model.JsonApiObject.NumberValue
import org.zalando.jsonapi.model.RootObject.ResourceObject

import scala.collection.immutable

//case class CassandraLimitOffsetPaginator[A](page: Int, size: Int, total: Long, items: Seq[A]) {
//  require(0 < page, "Page must be positive")
//
//  def limit: Int = page * size
//
//  def offset: Int = (page - 1) * size
//}
//
//object CassandraLimitOffsetPaginator {
//  implicit def format[A: Format]: Format[CassandraLimitOffsetPaginator[A]] = {
//    (
//      (__ \ "items").format[Seq[A]] and
//        (__ \ "page").format[Int] and
//        (__ \ "pageSize").format[Int] and
//        (__ \ "total").format[Long]
//      ).apply(CassandraLimitOffsetPaginator.apply, unlift(CassandraLimitOffsetPaginator.unapply))
//  }
//}

object Paginator {
//  implicit def format[T: Format]: Format[Paginator[T]] = {
//    (
//      (__ \ "data" \ 0 \ "type" ).format[String] and
//        (__ \ "meta" \ "page").format[Int] and
//        (__ \ "meta" \ "size").format[Int] and
//        (__ \ "meta" \ "total").format[Long] and
//        (__ \ "data").format[Seq[T]]
//      ).apply(
//      Paginator.apply,
//      unlift(Paginator.unapply)
//    )
//  }

  implicit def uF[T <: JsonApiTypeable]: Format[Paginator[T]] = {
    new Format[Paginator[T]] {
      override def reads(json: JsValue): JsResult[Paginator[T]] =
        Json.fromJson[Paginator[T]](json)

      override def writes(o: Paginator[T]) = Json.toJson(ResourceObject("s"))(org.zalando.jsonapi.json.playjson
      .PlayJsonJsonapiSupport.resourceObjectFormat)

//      override def writes(p: Paginator[T]): JsValue =
//        Json.toJson(
//          RootObject(
//            Some(
//              ResourceObjects(
//                immutable.Seq(
//                  p.items.map(o =>
//                    ResourceObject(
//                      o.getType,
//                      Some(o.getId))): _*))),
//            None,
//            None,
//            Some(Map[String, JsonApiObject.Value](
//              "page" -> NumberValue(p.page),
//              "size" -> NumberValue(p.size)))))
    }
  }
}



case class Paginator[T <: JsonApiTypeable](page: Int,
                                           size: Int,
                                           total: Long,
                                           items: Seq[T])
{
  require(0 < page, "page must be positive")
  require(0 < size, "size must be positive")

  def isEmpty: Boolean = items.isEmpty

  def isFirst: Boolean = page == 0

  def isLast: Boolean = total <= (page + 1) * size

  def isPaged: Boolean = total > size

  def pageCount: Long = ((total - 1) / size) + 1

  def limit: Int = page * size

  def offset: Int = (page - 1) * size
}
