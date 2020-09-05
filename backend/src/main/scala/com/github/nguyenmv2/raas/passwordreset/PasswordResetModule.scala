package com.github.nguyenmv2.raas.passwordreset

import com.github.nguyenmv2.raas.email.{EmailScheduler, EmailTemplates}
import com.github.nguyenmv2.raas.http.Http
import com.github.nguyenmv2.raas.security.Auth
import com.github.nguyenmv2.raas.user.UserModel
import com.github.nguyenmv2.raas.util.BaseModule
import doobie.util.transactor.Transactor
import monix.eval.Task

trait PasswordResetModule extends BaseModule {
  lazy val passwordResetCodeModel = new PasswordResetCodeModel
  lazy val passwordResetService =
    new PasswordResetService(
      userModel,
      passwordResetCodeModel,
      emailScheduler,
      emailTemplates,
      passwordResetCodeAuth,
      idGenerator,
      config.passwordReset,
      clock,
      xa
    )
  lazy val passwordResetApi = new PasswordResetApi(http, passwordResetService, xa)

  def userModel: UserModel
  def http: Http
  def passwordResetCodeAuth: Auth[PasswordResetCode]
  def emailScheduler: EmailScheduler
  def emailTemplates: EmailTemplates
  def xa: Transactor[Task]
}
