package com.github.nguyenmv2.raas.account

import java.time.Clock

import com.github.nguyenmv2.raas.config.Config
import com.github.nguyenmv2.raas.MainModule
import com.github.nguyenmv2.raas.test.{BaseTest, Requests, TestConfig, TestEmbeddedPostgres}
import monix.eval.Task
import com.github.nguyenmv2.raas.infrastructure.Doobie._
import com.github.nguyenmv2.raas.infrastructure.Json._
import org.http4s.Status
import org.scalatest.concurrent.Eventually
import sttp.client.impl.monix.TaskMonadAsyncError
import sttp.client.testing.SttpBackendStub
import sttp.client.{NothingT, SttpBackend}
import com.github.nguyenmv2.raas.account.AccountApi._

class AccountApiTest extends BaseTest with TestEmbeddedPostgres with Eventually {
  lazy val modules: MainModule = new MainModule {
    override def xa: Transactor[Task] = currentDb.xa
    override lazy val baseSttpBackend: SttpBackend[Task, Nothing, NothingT] = SttpBackendStub(TaskMonadAsyncError)
    override lazy val config: Config = TestConfig
    override lazy val clock: Clock = testClock
  }

  val requests = new Requests(modules)
  import requests._ 

  "/account/create" should "create" in {
    val name = randomAccountName()

    val response = createAccount(name)

    response.status shouldBe Status.Ok
    val accountId = response.shouldDeserializeTo[CreateAccount_OUT].id

    val getAccResp = getAccount(accountId)

    getAccResp.status shouldBe Status.Ok
    val result = getAccResp.shouldDeserializeTo[GetAccount_OUT]
    result.id shouldBe accountId
    result.name shouldBe name
  }
}
