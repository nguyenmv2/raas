package com.github.nguyenmv2.raas.user

import java.time.Instant

import cats.data.NonEmptyList
import com.github.nguyenmv2.raas.http.Http
import com.github.nguyenmv2.raas.infrastructure.Json._
import com.github.nguyenmv2.raas.infrastructure.Doobie._
import com.github.nguyenmv2.raas.metrics.Metrics
import com.github.nguyenmv2.raas.security.{ApiKey, Auth}
import com.github.nguyenmv2.raas.util.{ ServerEndpoints, Id }
import doobie.util.transactor.Transactor
import monix.eval.Task
import com.softwaremill.tagging.@@

import scala.concurrent.duration._
import com.github.nguyenmv2.raas.account.Account

class UserApi(http: Http, auth: Auth[ApiKey], userService: UserService, xa: Transactor[Task]) {
  import UserApi._
  import http._

  private val UserPath = "user"

  private val registerUserEndpoint = baseEndpoint.post
    .in(UserPath / "register")
    .in(jsonBody[Register_IN])
    .out(jsonBody[Register_OUT])
    .serverLogic { data =>
      (for {
        apiKey <- userService.registerNewUser(data.login, data.email, data.password, data.account_id).transact(xa)
        _ <- Task(Metrics.registeredUsersCounter.inc())
      } yield Register_OUT(apiKey.id)).toOut
    }

  private val loginEndpoint = baseEndpoint.post
    .in(UserPath / "login")
    .in(jsonBody[Login_IN])
    .out(jsonBody[Login_OUT])
    .serverLogic { data =>
      (for {
        apiKey <- userService
          .login(data.loginOrEmail, data.password, data.apiKeyValidHours.map(h => Duration(h.toLong, HOURS)))
          .transact(xa)
      } yield Login_OUT(apiKey.id)).toOut
    }

  private val changePasswordEndpoint = secureEndpoint.post
    .in(UserPath / "changepassword")
    .in(jsonBody[ChangePassword_IN])
    .out(jsonBody[ChangePassword_OUT])
    .serverLogic {
      case (authData, data) =>
        (for {
          userId <- auth(authData)
          _ <- userService.changePassword(userId, data.currentPassword, data.newPassword).transact(xa)
        } yield ChangePassword_OUT()).toOut
    }

  private val getUserEndpoint = secureEndpoint.get
    .in(UserPath)
    .out(jsonBody[GetUser_OUT])
    .serverLogic { authData =>
      (for {
        userId <- auth(authData)
        user <- userService.findById(userId).transact(xa)
      } yield GetUser_OUT(user.login, user.emailLowerCased, user.createdOn)).toOut
    }

  private val updateUserEndpoint = secureEndpoint.post
    .in(UserPath)
    .in(jsonBody[UpdateUser_IN])
    .out(jsonBody[UpdateUser_OUT])
    .serverLogic {
      case (authData, data) =>
        (for {
          userId <- auth(authData)
          _ <- userService.changeUser(userId, data.login, data.email).transact(xa)
        } yield UpdateUser_OUT()).toOut
    }

  val endpoints: ServerEndpoints =
    NonEmptyList
      .of(
        registerUserEndpoint,
        loginEndpoint,
        changePasswordEndpoint,
        getUserEndpoint,
        updateUserEndpoint
      )
      .map(_.tag("user"))
}

object UserApi {
  case class Register_IN(login: String, email: String, password: String, account_id: Id @@ Account)
  case class Register_OUT(apiKey: String)

  case class ChangePassword_IN(currentPassword: String, newPassword: String)
  case class ChangePassword_OUT()

  case class Login_IN(loginOrEmail: String, password: String, apiKeyValidHours: Option[Int])
  case class Login_OUT(apiKey: String)

  case class UpdateUser_IN(login: String, email: String)
  case class UpdateUser_OUT()

  case class GetUser_OUT(login: String, email: String, createdOn: Instant)
}
