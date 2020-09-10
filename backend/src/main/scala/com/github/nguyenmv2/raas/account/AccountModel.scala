package com.github.nguyenmv2.raas.account

import java.time.Instant

import cats.implicits._
import com.github.nguyenmv2.raas.infrastructure.Doobie._
import com.github.nguyenmv2.raas.util.Id
import com.softwaremill.tagging.@@

class AccountModel {
  def insert(account: Account): ConnectionIO[Unit] = {
    sql"""INSERT INTO accounts (id, name)
         |VALUES (${account.id}, ${account.name})""".stripMargin.update.run.void
  }

  def findById(id: Id @@ Account): ConnectionIO[Option[Account]] = {
    findBy(fr"id = $id")
  }

  private def findBy(by: Fragment): ConnectionIO[Option[Account]] = {
    (sql"SELECT id, name, created_at, updated_at FROM accounts WHERE " ++ by)
      .query[Account]
      .option
  }
}

case class Account(
    id: Id @@ Account,
    name: String,
    createdAt: Instant,
    updatedAt: Instant
)
