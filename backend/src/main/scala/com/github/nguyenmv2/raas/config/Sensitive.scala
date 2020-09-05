package com.github.nguyenmv2.raas.config

case class Sensitive(value: String) extends AnyVal {
  override def toString: String = "***"
}
