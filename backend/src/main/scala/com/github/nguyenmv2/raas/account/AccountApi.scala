package com.github.nguyenmv2.raas.account

import cats.data.NonEmptyList
import com.github.nguyenmv2.raas.http.Http
import com.github.nguyenmv2.raas.infrastructure.Doobie._
import com.github.nguyenmv2.raas.metrics.Metrics
import com.github.nguyenmv2.raas.util.{ ServerEndpoints, Id }
import com.github.nguyenmv2.raas.infrastructure.Json._
import doobie.util.transactor.Transactor
import monix.eval.Task
import com.softwaremill.tagging.@@

import scala.concurrent.duration._
import java.time.Instant

class AccountApi(http: Http, accountService: AccountService, xa: Transactor[Task]) {
  import AccountApi._
  import http._

  private val AccountPath = "account"

  private val registerAccountEndpoint =
    baseEndpoint
      .post
      .in(AccountPath / "create")
      .in(jsonBody[CreateAccount_IN])
      .out(jsonBody[CreateAccount_OUT])
      .serverLogic { data => 
        (
          for {
            accountId <- accountService.registerNewAccount(data.name).transact(xa)
            _ <- Task(Metrics.registeredAccountsCounter.inc())
          } yield CreateAccount_OUT(accountId)).toOut
      }

  private val getAccountEndpoint =
      baseEndpoint.get
        .in(AccountPath)
        .in(path[String]("accountId"))
        .out(jsonBody[GetAccount_OUT])
      .serverLogic { accountId => 
        (for {
          account <- accountService.findById(accountId.asInstanceOf[Id @@ Account]).transact(xa)
        } yield GetAccount_OUT(account.id, account.name, account.createdAt, account.updatedAt)
        ).toOut
      }
  val endpoints: ServerEndpoints =
    NonEmptyList
      .of(
        registerAccountEndpoint,
        getAccountEndpoint
      )
      .map(_.tag("account"))
}

object AccountApi {
  case class CreateAccount_IN(name: String)
  case class CreateAccount_OUT(id: Id @@ Account)
  case class GetAccount_OUT(id: Id @@ Account, name: String, createdAt: Instant, updatedAt: Instant)
}
