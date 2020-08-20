package org.datumbrain.hellohttp4s

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import io.circe.{Encoder, Decoder, Json, HCursor}
import io.circe.generic.semiauto._
import org.http4s._
import org.http4s.implicits._
import org.http4s.{EntityDecoder, EntityEncoder, Method, Uri, Request}
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.Method._
import org.http4s.circe._

trait TrumpQuotes[F[_]]{
  def get: F[TrumpQuotes.Quote]
}

object TrumpQuotes {
  def apply[F[_]](implicit ev: TrumpQuotes[F]): TrumpQuotes[F] = ev

  final case class Quote(value: String) extends AnyVal
  object Quote {
    implicit val quoteDecoder: Decoder[Quote] = deriveDecoder[Quote]
    implicit def quoteEntityDecoder[F[_]: Sync]: EntityDecoder[F, Quote] =
      jsonOf
    implicit val quoteEncoder: Encoder[Quote] = deriveEncoder[Quote]
    implicit def quoteEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Quote] =
      jsonEncoderOf
  }

  final case class QuoteError(e: Throwable) extends RuntimeException

  def impl[F[_]: Sync](C: Client[F]): TrumpQuotes[F] = new TrumpQuotes[F]{
    val dsl = new Http4sClientDsl[F]{}
    import dsl._
    def get: F[TrumpQuotes.Quote] = {
      C.expect[Quote](GET(uri"https://api.tronalddump.io/random/quote/"))
        .adaptError{ case t => QuoteError(t)} // Prevent Client Json Decoding Failure Leaking
    }
  }
}