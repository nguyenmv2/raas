package com.github.nguyenmv2.raas.account

import com.github.nguyenmv2.raas.util.BaseModule
import doobie.util.transactor.Transactor
import monix.eval.Task
import com.github.nguyenmv2.raas.http.Http

trait AccountModule extends BaseModule {
  lazy val accountModel = new AccountModel
  lazy val accountApi = new AccountApi(http, accountService, xa)
  lazy val accountService = new AccountService(accountModel, idGenerator, clock)

  def http: Http
  def xa: Transactor[Task]
}

