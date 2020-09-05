package com.github.nguyenmv2.raas.config

import com.github.nguyenmv2.raas.email.EmailConfig
import com.github.nguyenmv2.raas.http.HttpConfig
import com.github.nguyenmv2.raas.infrastructure.DBConfig
import com.github.nguyenmv2.raas.passwordreset.PasswordResetConfig
import com.github.nguyenmv2.raas.user.UserConfig

/**
  * Maps to the `application.conf` file. Configuration for all modules of the application.
  */
case class Config(db: DBConfig, api: HttpConfig, email: EmailConfig, passwordReset: PasswordResetConfig, user: UserConfig)
