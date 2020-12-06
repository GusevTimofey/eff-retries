package com.gusev.timofey.models

import cats.effect.Timer
import cats.{Applicative, Apply, Monad}
import com.gusev.timofey.models.RetryDecision.{GiveUp, PerformNextRetry}
import tofu.optics.Contains
import tofu.syntax.handle._
import tofu.syntax.monadic._
import tofu.syntax.raise._
import tofu.{Catches, Throws}

import scala.concurrent.duration.{Duration, FiniteDuration}

sealed trait RetryPolicy[F[+_]] {

  def nextRetryDecision: RetryState => F[RetryDecision]

  def retry[A](
    fa: F[A],
    logError: (Throwable, RetryDecision) => F[Unit]
  )(implicit C: Catches[F], Th: Throws[F], M: Monad[F], T: Timer[F]): F[A] = {
    def loop(state: RetryState): F[A] =
      fa.handleWith { err: Throwable =>
        nextRetryDecision(state)
          .flatTap(logError(err, _))
          .flatMap {
            case PerformNextRetry(inTime) =>
              T.sleep(inTime) >> loop(Contains[RetryState, Long].update(state, _ + 1))
            case GiveUp                   =>
              err.raise
          }
      }

    loop(RetryState.empty)
  }
}

object RetryPolicy {

  final case class ConstantTimePolicy[F[+_]](constantDelay: FiniteDuration)(implicit
    A: Applicative[F]
  ) extends RetryPolicy[F] {

    def nextRetryDecision: RetryState => F[RetryDecision] =
      _ => PerformNextRetry(constantDelay).pure[F]
  }

  final case class LimitNumberPolicy[F[+_]](maxRetries: Long)(implicit
    A: Applicative[F]
  ) extends RetryPolicy[F] {

    def nextRetryDecision: RetryState => F[RetryDecision] =
      state =>
        Either
          .cond(
            state.retriesNumber < maxRetries,
            PerformNextRetry(Duration.Zero),
            GiveUp
          )
          .merge
          .pure[F]
  }

  def merge[F[+_]](policy0: RetryPolicy[F], policy1: RetryPolicy[F])(implicit
    A: Apply[F]
  ): RetryPolicy[F] =
    new RetryPolicy[F] {
      def nextRetryDecision: RetryState => F[RetryDecision] =
        state =>
          (policy0.nextRetryDecision(state) map2 policy1.nextRetryDecision(
            state
          )) {
            case (PerformNextRetry(time0), PerformNextRetry(time1)) =>
              PerformNextRetry(time0 max time1)
            case _                                                  => GiveUp
          }

    }
}
