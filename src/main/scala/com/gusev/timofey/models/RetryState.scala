package com.gusev.timofey.models
import tofu.optics.macros.ClassyOptics

@ClassyOptics
final case class RetryState(retriesNumber: Long)

object RetryState {
  def empty: RetryState = RetryState(0)
}
