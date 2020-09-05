package com.github.nguyenmv2.raas.email.sender

import com.github.nguyenmv2.raas.email.EmailData
import com.github.nguyenmv2.raas.test.BaseTest
import monix.execution.Scheduler.Implicits.global

class DummyEmailSenderTest extends BaseTest {
  it should "send scheduled email" in {
    DummyEmailSender(EmailData("test@sml.com", "subject", "content")).runSyncUnsafe()
    DummyEmailSender.findSentEmail("test@sml.com", "subject").isDefined shouldBe true
  }
}
