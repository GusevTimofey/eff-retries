package com.gusev.timofey.syntax
import cats.Apply
import com.gusev.timofey.models.RetryPolicy
import com.gusev.timofey.syntax.combine.CombineOps

trait CombineSyntax {
  implicit def combineSyntax[F[+_]](policy0: RetryPolicy[F]): CombineOps[F] = new CombineOps[F](policy0)
}

object combine {
  implicit final class CombineOps[F[+_]](val policy0: RetryPolicy[F]) extends AnyVal {
    def merge(policy1: RetryPolicy[F])(implicit A: Apply[F]): RetryPolicy[F] = RetryPolicy.merge(policy0, policy1)
  }
}
