package com.gusev.timofey.syntax
import cats.Monad
import cats.effect.Timer
import com.gusev.timofey.models.{RetryDecision, RetryPolicy}
import tofu.{Catches, Throws}
import retries._

trait RetriesOnAll {
  implicit def syntaxRetryOps[F[+_], A](fa: F[A]): RetryOps[F, A] = RetryOps[F, A](fa)
}

object retries {

  implicit final class RetryOps[F[+_], A](val fa: F[A]) extends AnyVal {
    def retryOnAll(policy: RetryPolicy[F], logError: (Throwable, RetryDecision) => F[Unit])(implicit
      C: Catches[F],
      Th: Throws[F],
      M: Monad[F],
      T: Timer[F]
    ): F[A] =
      policy.retry(fa, logError)
  }

}
