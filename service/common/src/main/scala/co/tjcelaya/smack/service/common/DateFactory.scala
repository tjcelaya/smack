package co.tjcelaya.smack.service.common

import java.time.ZonedDateTime
import java.util.Date

/**
  * Created by tj on 3/2/17.
  */
object DateFactory {
  def now: Date = {
    Date.from(ZonedDateTime.now.toInstant)
  }
}
