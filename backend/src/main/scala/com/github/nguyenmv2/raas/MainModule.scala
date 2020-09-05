package com.github.nguyenmv2.raas

import java.time.Clock

import cats.data.NonEmptyList
import com.github.nguyenmv2.raas.email.EmailModule
import com.github.nguyenmv2.raas.http.{Http, HttpApi}
import com.github.nguyenmv2.raas.infrastructure.InfrastructureModule
import com.github.nguyenmv2.raas.metrics.MetricsModule
import com.github.nguyenmv2.raas.passwordreset.PasswordResetModule
import com.github.nguyenmv2.raas.security.SecurityModule
import com.github.nguyenmv2.raas.user.UserModule
import com.github.nguyenmv2.raas.util.{DefaultIdGenerator, IdGenerator, ServerEndpoints}
import monix.eval.Task

/**
  * Main application module. Depends on resources initialised in [[InitModule]].
  */
trait MainModule
    extends SecurityModule
    with EmailModule
    with UserModule
    with PasswordResetModule
    with MetricsModule
    with InfrastructureModule {

  override lazy val idGenerator: IdGenerator = DefaultIdGenerator
  override lazy val clock: Clock = Clock.systemUTC()

  lazy val http: Http = new Http()

  private lazy val endpoints: ServerEndpoints = userApi.endpoints concatNel passwordResetApi.endpoints
  private lazy val adminEndpoints: ServerEndpoints = NonEmptyList.of(metricsApi.metricsEndpoint, versionApi.versionEndpoint)

  lazy val httpApi: HttpApi = new HttpApi(http, endpoints, adminEndpoints, collectorRegistry, config.api)

  lazy val startBackgroundProcesses: Task[Unit] = emailService.startProcesses().void
}
