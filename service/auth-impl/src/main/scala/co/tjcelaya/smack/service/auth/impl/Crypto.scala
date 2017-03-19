package co.tjcelaya.smack.service.auth.impl

import com.lambdaworks.crypto.SCryptUtil

/**
  * Created by tj on 3/18/17.
  */
object Crypto {

  def passwordHash(password: String): String = {
    SCryptUtil.scrypt(password, 16, 1, 1)
  }

  def passwordCheck(password: String, expected: String): Boolean = {
    SCryptUtil.check(password, expected)
  }
}
