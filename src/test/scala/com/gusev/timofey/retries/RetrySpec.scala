package com.gusev.timofey.retries

import cats.effect.{IO, Timer}
import com.gusev.timofey.models.RetryDecision.{GiveUp, PerformNextRetry}
import com.gusev.timofey.models.RetryPolicy._
import com.gusev.timofey.models.{RetryDecision, RetryState}
import com.gusev.timofey.syntax.all._
import org.specs2.mutable.Specification

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

class RetrySpec extends Specification {

  "Retry policy should" >> {
    "choose correct strategy" >> {
      val constantTimePolicy = ConstantTimePolicy[IO](Duration.Zero)
      constantTimePolicy.nextRetryDecision(RetryState.empty).unsafeRunSync() mustEqual PerformNextRetry(
        Duration.Zero
      )

      val limitNumberPolicy = LimitNumberPolicy[IO](2)
      limitNumberPolicy.nextRetryDecision(RetryState.empty).unsafeRunSync() mustEqual PerformNextRetry(
        Duration.Zero
      )
      limitNumberPolicy.nextRetryDecision(RetryState(2)).unsafeRunSync() mustEqual GiveUp

      val mergedPolicy = constantTimePolicy.merge(limitNumberPolicy)
      mergedPolicy.nextRetryDecision(RetryState.empty).unsafeRunSync() mustEqual PerformNextRetry(
        Duration.Zero
      )
      mergedPolicy.nextRetryDecision(RetryState(2)).unsafeRunSync() mustEqual GiveUp

      def f: (Throwable, RetryDecision) => IO[Unit] =
        (err, decision) => IO.delay(println(s"Test logging. Err: ${err.getMessage}, decision: $decision."))

      implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

      IO[Int](throw new Exception("Test exception"))
        .retryOnAll(mergedPolicy, f)
        .unsafeRunSync() must throwA[Throwable]
    }

  }
}
