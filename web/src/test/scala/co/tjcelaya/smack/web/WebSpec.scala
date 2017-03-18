package co.tjcelaya.smack.web

import org.scalatest._

class WebSpec extends FlatSpec with Matchers {
  "The Hello object" should "say hello" in {
    Web.getClass shouldEqual "WebServer"
  }
}
