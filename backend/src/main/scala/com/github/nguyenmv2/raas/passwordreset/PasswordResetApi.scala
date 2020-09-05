package com.github.nguyenmv2.raas.passwordreset

import cats.data.NonEmptyList
import com.github.nguyenmv2.raas.http.Http
import com.github.nguyenmv2.raas.infrastructure.Json._
import com.github.nguyenmv2.raas.infrastructure.Doobie._
import com.github.nguyenmv2.raas.util.ServerEndpoints
import doobie.util.transactor.Transactor
import monix.eval.Task

class PasswordResetApi(http: Http, passwordResetService: PasswordResetService, xa: Transactor[Task]) {
  import PasswordResetApi._
  import http._

  private val PasswordResetPath = "passwordreset"

  private val passwordResetEndpoint = baseEndpoint.post
    .in(PasswordResetPath / "reset")
    .in(jsonBody[PasswordReset_IN])
    .out(jsonBody[PasswordReset_OUT])
    .serverLogic { data =>
      (for {
        _ <- passwordResetService.resetPassword(data.code, data.password)
      } yield PasswordReset_OUT()).toOut
    }

  private val forgotPasswordEndpoint = baseEndpoint.post
    .in(PasswordResetPath / "forgot")
    .in(jsonBody[ForgotPassword_IN])
    .out(jsonBody[ForgotPassword_OUT])
    .serverLogic { data =>
      (for {
        _ <- passwordResetService.forgotPassword(data.loginOrEmail).transact(xa)
      } yield ForgotPassword_OUT()).toOut
    }

  val endpoints: ServerEndpoints =
    NonEmptyList
      .of(
        passwordResetEndpoint,
        forgotPasswordEndpoint
      )
      .map(_.tag("passwordreset"))
}

object PasswordResetApi {
  case class PasswordReset_IN(code: String, password: String)
  case class PasswordReset_OUT()

  case class ForgotPassword_IN(loginOrEmail: String)
  case class ForgotPassword_OUT()
}
