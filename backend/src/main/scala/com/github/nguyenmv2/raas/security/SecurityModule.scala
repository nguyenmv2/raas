package com.github.nguyenmv2.raas.security

import com.github.nguyenmv2.raas.passwordreset.{PasswordResetAuthToken, PasswordResetCode, PasswordResetCodeModel}
import com.github.nguyenmv2.raas.util.BaseModule
import doobie.util.transactor.Transactor
import monix.eval.Task

trait SecurityModule extends BaseModule {
  lazy val apiKeyModel = new ApiKeyModel
  lazy val apiKeyService = new ApiKeyService(apiKeyModel, idGenerator, clock)
  lazy val apiKeyAuth: Auth[ApiKey] = new Auth(new ApiKeyAuthToken(apiKeyModel), xa, clock)
  lazy val passwordResetCodeAuth: Auth[PasswordResetCode] = new Auth(new PasswordResetAuthToken(passwordResetCodeModel), xa, clock)

  def passwordResetCodeModel: PasswordResetCodeModel
  def xa: Transactor[Task]
}
