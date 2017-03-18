package co.tjcelaya.smack.service.common

/**
  * Created by tj on 2/26/17.
  */
trait JsonApiTypeable {
  def getType: String
  def getId: String
}
