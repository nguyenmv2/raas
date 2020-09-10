package com.github.nguyenmv2.raas.account

import java.time.Clock

import cats.implicits._
import com.github.nguyenmv2.raas._
import com.typesafe.scalalogging.StrictLogging
import com.softwaremill.tagging.@@
import com.github.nguyenmv2.raas.infrastructure.Doobie._
import com.github.nguyenmv2.raas.util._
import doobie.free.driver.DriverOp.Connect

class AccountService(
    accountModel: AccountModel,
    idGenerator: IdGenerator,
    clock: Clock
) extends StrictLogging {

  def registerNewAccount(name: String): ConnectionIO[Id @@ Account] = {
    def doRegister(): ConnectionIO[Id @@ Account] = {
      val account = Account(idGenerator.nextId[Account](), name, clock.instant(), clock.instant())

      logger.debug(s"Registering new account: ${account.name} with id: ${account.id}")
      for {
        _ <- accountModel.insert(account)
      } yield account.id
    }

    for {
      _ <-
        AccountRegisterValidator
          .validate(name)
          .fold(msg => Fail.IncorrectInput(msg).raiseError[ConnectionIO, Unit], _ => ().pure[ConnectionIO])
      accountId <- doRegister()
    } yield accountId
  }

  def findById(id: Id @@ Account): ConnectionIO[Account] = {
    accountModel.findById(id).flatMap{
      case Some(account) => account.pure[ConnectionIO]
      case None          => Fail.NotFound("account").raiseError[ConnectionIO, Account]
    }
  }
}

object AccountRegisterValidator {
  private val ValidationOk = Right(())

  def validate(name: String): Either[String, Unit] = {
    for {
      _ <- validName(name)
    } yield ()
  }

  private def validName(name: String) = {
    if (name.nonEmpty) ValidationOk
    else Left("Name cannot be empty")
  }
}
