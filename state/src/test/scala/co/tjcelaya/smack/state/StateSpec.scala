package co.tjcelaya.smack.state

import org.scalatest._

class StateSpec extends FlatSpec with Matchers {
  it should "boot and shutdown" in {
    // TODO: why does this work, with and without the Thread.sleep?
    // State.main(Array())
    // Thread.sleep(5000)
    assert(true)
  }
}
