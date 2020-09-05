package com.github.nguyenmv2.raas.user

import com.github.nguyenmv2.raas.email.{EmailScheduler, EmailTemplates}
import com.github.nguyenmv2.raas.http.Http
import com.github.nguyenmv2.raas.security.{ApiKey, ApiKeyService, Auth}
import com.github.nguyenmv2.raas.util.BaseModule
import doobie.util.transactor.Transactor
import monix.eval.Task

trait UserModule extends BaseModule {
  lazy val userModel = new UserModel
  lazy val userApi = new UserApi(http, apiKeyAuth, userService, xa)
  lazy val userService = new UserService(userModel, emailScheduler, emailTemplates, apiKeyService, idGenerator, clock, config.user)

  def http: Http
  def apiKeyAuth: Auth[ApiKey]
  def emailScheduler: EmailScheduler
  def emailTemplates: EmailTemplates
  def apiKeyService: ApiKeyService
  def xa: Transactor[Task]
}
