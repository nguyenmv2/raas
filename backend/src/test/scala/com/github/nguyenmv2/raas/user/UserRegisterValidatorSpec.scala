package com.github.nguyenmv2.raas.user

import com.github.nguyenmv2.raas.util.Id
import com.github.nguyenmv2.raas.account.Account
import com.softwaremill.tagging.@@

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class UserRegisterValidatorSpec extends AnyFlatSpec with Matchers {
  "validate" should "accept valid data" in {
    val dataIsValid = UserRegisterValidator.validate("login", "admin@raas.com", "password", "AccountID".asInstanceOf[Id @@ Account])

    dataIsValid shouldBe Right(())
  }

  "validate" should "not accept login containing only empty spaces" in {
    val dataIsValid = UserRegisterValidator.validate("   ", "admin@raas.com", "password", "AccountID".asInstanceOf[Id @@ Account])

    dataIsValid.isLeft shouldBe true
  }

  "validate" should "not accept too short login" in {
    val tooShortLogin = "a" * (UserRegisterValidator.MinLoginLength - 1)
    val dataIsValid   = UserRegisterValidator.validate(tooShortLogin, "admin@raas.com", "password", "AccountID".asInstanceOf[Id @@ Account])

    dataIsValid.isLeft shouldBe true
  }

  "validate" should "not accept too short login after trimming" in {
    val loginTooShortAfterTrim = "a" * (UserRegisterValidator.MinLoginLength - 1) + "   "
    val dataIsValid            = UserRegisterValidator.validate(loginTooShortAfterTrim, "admin@raas.com", "password", "AccountID".asInstanceOf[Id @@ Account])

    dataIsValid.isLeft shouldBe true
  }

  "validate" should "not accept missing email with spaces only" in {
    val dataIsValid = UserRegisterValidator.validate("login", "   ", "password", "AccountID".asInstanceOf[Id @@ Account])

    dataIsValid.isLeft shouldBe true
  }

  "validate" should "not accept invalid email" in {
    val dataIsValid = UserRegisterValidator.validate("login", "invalidEmail", "password", "AccountID".asInstanceOf[Id @@ Account])

    dataIsValid.isLeft shouldBe true
  }

  "validate" should "not accept password with empty spaces only" in {
    val dataIsValid = UserRegisterValidator.validate("login", "admin@raas.com", "    ", "AccountID".asInstanceOf[Id @@ Account])

    dataIsValid.isLeft shouldBe true
  }
}
