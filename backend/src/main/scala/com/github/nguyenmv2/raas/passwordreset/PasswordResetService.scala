package com.github.nguyenmv2.raas.passwordreset

import java.time.Clock
import java.time.temporal.ChronoUnit

import cats.implicits._
import com.github.nguyenmv2.raas._
import com.github.nguyenmv2.raas.email.{EmailData, EmailScheduler, EmailSubjectContent, EmailTemplates}
import com.github.nguyenmv2.raas.infrastructure.Doobie._
import com.github.nguyenmv2.raas.security.Auth
import com.github.nguyenmv2.raas.user.{User, UserModel}
import com.github.nguyenmv2.raas.util._
import com.typesafe.scalalogging.StrictLogging
import monix.eval.Task

class PasswordResetService(
    userModel: UserModel,
    passwordResetCodeModel: PasswordResetCodeModel,
    emailScheduler: EmailScheduler,
    emailTemplates: EmailTemplates,
    auth: Auth[PasswordResetCode],
    idGenerator: IdGenerator,
    config: PasswordResetConfig,
    clock: Clock,
    xa: Transactor[Task]
) extends StrictLogging {

  def forgotPassword(loginOrEmail: String): ConnectionIO[Unit] = {
    userModel.findByLoginOrEmail(loginOrEmail.lowerCased).flatMap {
      case None => Fail.NotFound("user").raiseError[ConnectionIO, Unit]
      case Some(user) =>
        createCode(user).flatMap(sendCode(user, _))
    }
  }

  private def createCode(user: User): ConnectionIO[PasswordResetCode] = {
    logger.debug(s"Creating password reset code for user: ${user.id}")
    val validUntil = clock.instant().plus(config.codeValid.toMinutes, ChronoUnit.MINUTES)
    val code = PasswordResetCode(idGenerator.nextId[PasswordResetCode](), user.id, validUntil)
    passwordResetCodeModel.insert(code).map(_ => code)
  }

  private def sendCode(user: User, code: PasswordResetCode): ConnectionIO[Unit] = {
    logger.debug(s"Scheduling e-mail with reset code for user: ${user.id}")
    emailScheduler(EmailData(user.emailLowerCased, prepareResetEmail(user, code)))
  }

  private def prepareResetEmail(user: User, code: PasswordResetCode): EmailSubjectContent = {
    val resetLink = String.format(config.resetLinkPattern, code.id)
    emailTemplates.passwordReset(user.login, resetLink)
  }

  def resetPassword(code: String, newPassword: String): Task[Unit] = {
    for {
      userId <- auth(code.asInstanceOf[Id])
      _ = logger.debug(s"Resetting password for user: $userId")
      _ <- userModel.updatePassword(userId, User.hashPassword(newPassword)).transact(xa)
    } yield ()
  }
}
