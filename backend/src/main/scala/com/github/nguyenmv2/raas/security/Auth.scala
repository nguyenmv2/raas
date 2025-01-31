package com.github.nguyenmv2.raas.security

import java.security.SecureRandom
import java.time.{Clock, Instant}

import cats.data.OptionT
import cats.effect.Timer
import com.github.nguyenmv2.raas.infrastructure.Doobie._
import com.github.nguyenmv2.raas.user.User
import com.github.nguyenmv2.raas._
import com.softwaremill.tagging._
import com.typesafe.scalalogging.StrictLogging
import monix.eval.Task
import com.github.nguyenmv2.raas.util._

import scala.concurrent.duration._

class Auth[T](
    authTokenOps: AuthTokenOps[T],
    xa: Transactor[Task],
    clock: Clock
) extends StrictLogging {

  // see https://hackernoon.com/hack-how-to-use-securerandom-with-kubernetes-and-docker-a375945a7b21
  private val random = SecureRandom.getInstance("NativePRNGNonBlocking")

  /**
    * Authenticates using the given authentication token. If the token is invalid, a failed [[Task]] is returned,
    * with an instance of the [[Fail]] class. Otherwise, the id of the authenticated user is given.
    */
  def apply(id: Id): Task[Id @@ User] = {
    val tokenOpt = (for {
      token <- OptionT(authTokenOps.findById(id.asId[T]).transact(xa))
      _ <- OptionT(verifyValid(token))
    } yield token).value

    tokenOpt.flatMap {
      case None =>
        logger.debug(s"Auth failed for: ${authTokenOps.tokenName} $id")
        // random sleep to prevent timing attacks
        Timer[Task].sleep(random.nextInt(1000).millis) >> Task.raiseError(Fail.Unauthorized)
      case Some(token) =>
        val delete = if (authTokenOps.deleteWhenValid) authTokenOps.delete(token).transact(xa) else Task.unit
        delete >> Task.now(authTokenOps.userId(token))
    }
  }

  private def verifyValid(token: T): Task[Option[Unit]] = {
    if (clock.instant().isAfter(authTokenOps.validUntil(token))) {
      logger.info(s"${authTokenOps.tokenName} expired: $token")
      authTokenOps.delete(token).transact(xa).map(_ => None)
    } else {
      Task(Some(()))
    }
  }
}

/**
  * A set of operations on an authentication token, which are performed during authentication. Supports both
  * one-time tokens (when `deleteWhenValid=true`) and multi-use tokens.
  */
trait AuthTokenOps[T] {
  def tokenName: String
  def findById: (Id @@ T) => ConnectionIO[Option[T]]
  def delete: T => ConnectionIO[Unit]
  def userId: T => Id @@ User
  def validUntil: T => Instant
  def deleteWhenValid: Boolean
}
