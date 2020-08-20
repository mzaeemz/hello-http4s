package org.datumbrain.hellohttp4s

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object Main extends IOApp {
  def run(args: List[String]) =
    Hellohttp4sServer.stream[IO].compile.drain.as(ExitCode.Success)
}