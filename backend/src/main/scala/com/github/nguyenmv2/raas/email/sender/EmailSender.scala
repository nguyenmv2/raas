package com.github.nguyenmv2.raas.email.sender

import com.github.nguyenmv2.raas.email.EmailData
import monix.eval.Task

trait EmailSender {
  def apply(email: EmailData): Task[Unit]
}
