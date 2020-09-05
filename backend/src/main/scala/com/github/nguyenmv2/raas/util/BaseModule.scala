package com.github.nguyenmv2.raas.util

import java.time.Clock

import com.github.nguyenmv2.raas.config.Config

trait BaseModule {
  def idGenerator: IdGenerator
  def clock: Clock
  def config: Config
}
