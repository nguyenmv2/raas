package com.github.nguyenmv2.raas.test

import com.github.nguyenmv2.raas.infrastructure.CorrelationId
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

trait BaseTest extends AnyFlatSpec with Matchers {
  CorrelationId.init()
  val testClock = new TestClock()
}
