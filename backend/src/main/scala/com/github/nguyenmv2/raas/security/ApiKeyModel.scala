package com.github.nguyenmv2.raas.security

import java.time.Instant

import cats.implicits._
import com.github.nguyenmv2.raas.infrastructure.Doobie
import com.github.nguyenmv2.raas.infrastructure.Doobie._
import com.github.nguyenmv2.raas.user.User
import com.github.nguyenmv2.raas.util.Id
import com.softwaremill.tagging.@@

class ApiKeyModel {

  def insert(apiKey: ApiKey): ConnectionIO[Unit] = {
    sql"""INSERT INTO api_keys (id, user_id, account_id, created_at, valid_until)
         |VALUES (${apiKey.id}, ${apiKey.userId}, ${apiKey.accountId}, ${apiKey.createdOn}, ${apiKey.validUntil})""".stripMargin.update.run.void
  }

  def findById(id: Id @@ ApiKey): ConnectionIO[Option[ApiKey]] = {
    sql"""SELECT id, user_id, account_id, created_at, valid_until FROM api_keys WHERE id = $id"""
      .query[ApiKey]
      .option
  }

  def delete(id: Id @@ ApiKey): ConnectionIO[Unit] = {
    sql"""DELETE FROM api_keys WHERE id = $id""".update.run.void
  }
}

class ApiKeyAuthToken(apiKeyModel: ApiKeyModel) extends AuthTokenOps[ApiKey] {
  override def tokenName: String = "ApiKey"
  override def findById: Id @@ ApiKey => Doobie.ConnectionIO[Option[ApiKey]] = apiKeyModel.findById
  override def delete: ApiKey => Doobie.ConnectionIO[Unit] = ak => apiKeyModel.delete(ak.id)
  override def userId: ApiKey => Id @@ User = _.userId
  override def validUntil: ApiKey => Instant = _.validUntil
  override def deleteWhenValid: Boolean = false
}
