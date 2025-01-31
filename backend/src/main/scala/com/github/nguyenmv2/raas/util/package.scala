package com.github.nguyenmv2.raas

import java.util.Locale

import cats.data.NonEmptyList
import com.softwaremill.tagging._
import monix.eval.Task
import sttp.tapir.server.ServerEndpoint
import tsec.common.SecureRandomId

package object util {
  type Id = SecureRandomId

  implicit class RichString(val s: String) extends AnyVal {
    def asId[T]: Id @@ T = s.asInstanceOf[Id @@ T]
    def lowerCased: String @@ LowerCased = s.toLowerCase(Locale.ENGLISH).taggedWith[LowerCased]
  }

  type ServerEndpoints = NonEmptyList[ServerEndpoint[_, _, _, Nothing, Task]]
}
