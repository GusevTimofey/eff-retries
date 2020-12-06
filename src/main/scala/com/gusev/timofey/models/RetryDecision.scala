package com.gusev.timofey.models

import derevo.derive
import tofu.logging.derivation.loggable

import scala.concurrent.duration.FiniteDuration

@derive(loggable)
sealed trait RetryDecision

object RetryDecision {
  @derive(loggable)
  final case class PerformNextRetry(inTime: FiniteDuration) extends RetryDecision
  @derive(loggable)
  final case object GiveUp extends RetryDecision
}
