package com.github.nguyenmv2.raas.test

import com.github.nguyenmv2.raas.MainModule
import com.github.nguyenmv2.raas.infrastructure.Json._
import com.github.nguyenmv2.raas.user.UserApi._
import com.github.nguyenmv2.raas.account.AccountApi._
import com.github.nguyenmv2.raas.account.Account
import com.github.nguyenmv2.raas.util.Id
import monix.eval.Task
import org.http4s._
import org.http4s.syntax.all._
import com.softwaremill.tagging.@@

import scala.util.Random

class Requests(val modules: MainModule) extends HttpTestSupport {
  case class RegisteredUser(login: String, email: String, password: String, apiKey: String)
  case class RegisteredAccount(id: Id @@ Account, name: String)

  private val random = new Random()

  def randomLoginEmailPassword(): (String, String, String) =
    (random.nextString(12), s"user${random.nextInt(9000)}@raas.com", random.nextString(12))

  def registerUser(login: String, email: String, password: String, accountId: Id @@ Account): Response[Task] = {
    val request = Request[Task](method = POST, uri = uri"/user/register")
      .withEntity(Register_IN(login, email, password, accountId))

    modules.httpApi.mainRoutes(request).unwrap
  }

  def newRegisteredUsed(): RegisteredUser = {
    val (login, email, password) = randomLoginEmailPassword()
    val apiKey = registerUser(login, email, password, registeredAccount().id).shouldDeserializeTo[Register_OUT].apiKey
    RegisteredUser(login, email, password, apiKey)
  }

  def loginUser(loginOrEmail: String, password: String, apiKeyValidHours: Option[Int] = None): Response[Task] = {
    val request = Request[Task](method = POST, uri = uri"/user/login")
      .withEntity(Login_IN(loginOrEmail, password, apiKeyValidHours))

    modules.httpApi.mainRoutes(request).unwrap
  }

  def getUser(apiKey: String): Response[Task] = {
    val request = Request[Task](method = GET, uri = uri"/user")
    modules.httpApi.mainRoutes(authorizedRequest(apiKey, request)).unwrap
  }

  def changePassword(apiKey: String, password: String, newPassword: String): Response[Task] = {
    val request = Request[Task](method = POST, uri = uri"/user/changepassword")
      .withEntity(ChangePassword_IN(password, newPassword))

    modules.httpApi.mainRoutes(authorizedRequest(apiKey, request)).unwrap
  }

  def updateUser(apiKey: String, login: String, email: String): Response[Task] = {
    val request = Request[Task](method = POST, uri = uri"/user")
      .withEntity(UpdateUser_IN(login, email))

    modules.httpApi.mainRoutes(authorizedRequest(apiKey, request)).unwrap
  }
  
  def registeredAccount(): RegisteredAccount = {
    val id = createAccount(randomAccountName()).shouldDeserializeTo[CreateAccount_OUT].id
    val result = getAccount(id).shouldDeserializeTo[GetAccount_OUT]
    RegisteredAccount(id = result.id, name = result.name)
  }

  def randomAccountName(): String = random.nextString(20)

  def createAccount(name: String): Response[Task] = {
    val request = Request[Task](method = POST, uri = uri"/account/create")
      .withEntity(CreateAccount_IN(name))

    modules.httpApi.mainRoutes(request).unwrap
  }

  def getAccount(id: Id @@ Account): Response[Task] = {
    val maybeUri = Uri.fromString(s"/account/${id}")
    maybeUri.fold(
      _ => Response.notFound, 
      uri => {
        val request = Request[Task](method = GET, uri = uri)

        modules.httpApi.mainRoutes(request).unwrap
      }
    )
  }
}
