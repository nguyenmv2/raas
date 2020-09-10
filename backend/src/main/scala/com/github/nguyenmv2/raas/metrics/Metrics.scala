package com.github.nguyenmv2.raas.metrics

import io.prometheus.client.{Counter, Gauge, hotspot}

object Metrics {
  lazy val registeredAccountsCounter: Counter =
    Counter
      .build()
      .name("raas_registerd_accounts_counter")
      .help("How many accounts registered on this instance since it was started")
      .register()

  lazy val registeredUsersCounter: Counter =
    Counter
      .build()
      .name(s"raas_registered_users_counter")
      .help(s"How many users registered on this instance since it was started")
      .register()

  lazy val emailQueueGauge: Gauge =
    Gauge
      .build()
      .name(s"raas_email_queue_gauge")
      .help(s"How many emails are waiting to be sent")
      .register()

  def init(): Unit = {
    hotspot.DefaultExports.initialize()
  }
}
