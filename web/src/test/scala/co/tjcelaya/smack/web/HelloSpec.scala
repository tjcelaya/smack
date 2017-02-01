package co.tjcelaya.smack.web

import org.scalatest._

import scala.web.Hello

class HelloSpec extends FlatSpec with Matchers {
  "The Hello object" should "say hello" in {
    Hello.greeting shouldEqual "hello"
  }
}
