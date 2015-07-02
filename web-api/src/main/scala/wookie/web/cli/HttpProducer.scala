package wookie.web.cli

import org.http4s.server.HttpService
import wookie.app.cli.BasicApp
import wookie.web.server.ServerRunner

abstract class HttpProducer[A <: Port](options: Array[String] => A) extends BasicApp[A](options) {
  
  def createServices(opt: A): Map[String, HttpService]

  final def run(opt: A): Unit = {
    val serverStart = for {
      s <- ServerRunner.start((createServices(opt), opt))
      _ <- ServerRunner.awaits(s)
    } yield s

    serverStart.unsafePerformIO()
    ()
  }
}