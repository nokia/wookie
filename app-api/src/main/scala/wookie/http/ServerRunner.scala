package wookie.http

import scalaz.effect._
import _root_.org.http4s.server.blaze.BlazeBuilder
import _root_.org.http4s.server._
import wookie.app.cli._

object ServerRunner {

  type ServiceConfiguration = (Map[String, HttpService], Port)

  def start: ServiceConfiguration => IO[Server] = env =>
    IO {
      def mountServices(builder: BlazeBuilder, services: List[(String, HttpService)]): BlazeBuilder = services match {
        case hd :: tail => mountServices(builder.mountService(hd._2, hd._1), tail)
        case _          => builder
      }
      val (services, conf) = env
      mountServices(BlazeBuilder, services.toList).bindHttp(conf.port(), "0.0.0.0").
        withNio2(true).run
    }

  def stop(server: Server) = IO {
    server.shutdownNow()
  }

  def restart(server: Server): ServiceConfiguration => IO[Server] = env =>
    for {
      _ <- stop(server)
      s <- start(env)
    } yield s

  def awaits(server: Server) = IO {
    server.awaitShutdown()
  }

}