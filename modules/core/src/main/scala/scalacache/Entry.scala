package scalacache

import java.time.Instant
import cats.effect.Clock
import java.util.concurrent.TimeUnit
import cats.implicits._
import language.higherKinds
import cats.Applicative

/** A cache entry with an optional expiry time
  */
case class Entry[+A](value: A, expiresAt: Option[Instant])

object Entry {

  /** Has the entry expired yet?
    */
  def isNotExpired[F[_], A](entry: Entry[A])(implicit clock: Clock[F], applicative: Applicative[F]): F[Boolean] =
    entry.expiresAt
      .traverse { expiration =>
        val now = clock.monotonic.map(m => Instant.ofEpochMilli(m.toMillis))

        now.map(expiration.isBefore(_))
      }
      .map {
        case Some(true)         => false
        case None | Some(false) => true
      }

  def isExpired[F[_], A](entry: Entry[A])(implicit clock: Clock[F], applicative: Applicative[F]): F[Boolean] =
    isNotExpired[F, A](entry).map(b => !b)
}
