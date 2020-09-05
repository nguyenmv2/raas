package com.github.nguyenmv2.raas.infrastructure

import com.github.nguyenmv2.raas.config.Sensitive

case class DBConfig(username: String, password: Sensitive, url: String, migrateOnStart: Boolean, driver: String, connectThreadPoolSize: Int)
