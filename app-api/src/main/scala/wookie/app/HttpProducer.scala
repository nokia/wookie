package wookie.app

import org.http4s.server._
import wookie.app.cli._
import wookie.http.ServerRunner

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