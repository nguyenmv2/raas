package com.github.nguyenmv2.raas.security

import java.time.{Clock, Instant}
import java.time.temporal.ChronoUnit

import com.github.nguyenmv2.raas.user.User
import com.softwaremill.tagging.@@
import com.github.nguyenmv2.raas.infrastructure.Doobie._
import com.github.nguyenmv2.raas.util.{Id, IdGenerator}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration.Duration
import com.github.nguyenmv2.raas.account.Account

class ApiKeyService(apiKeyModel: ApiKeyModel, idGenerator: IdGenerator, clock: Clock) extends StrictLogging {

  def create(userId: Id @@ User, accountId: Id @@ Account, valid: Duration): ConnectionIO[ApiKey] = {
    val now = clock.instant()
    val validUntil = now.plus(valid.toMinutes, ChronoUnit.MINUTES)
    val apiKey = ApiKey(idGenerator.nextId[ApiKey](), userId, accountId, now, validUntil)

    logger.debug(s"Creating a new api key for user $userId, valid until: $validUntil")
    apiKeyModel.insert(apiKey).map(_ => apiKey)
  }
}

case class ApiKey(id: Id @@ ApiKey, userId: Id @@ User, accountId: Id @@ Account, createdOn: Instant, validUntil: Instant)
