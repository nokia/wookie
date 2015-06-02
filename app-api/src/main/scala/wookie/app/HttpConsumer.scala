package wookie.app

import org.http4s.ParseFailure
import org.http4s.Request
import org.http4s.Uri

import com.ning.http.client.AsyncHttpClient
import com.ning.http.client.Response

import scalaz.Kleisli.kleisliFn
import scalaz.{\/ => \/}
import scalaz.concurrent.Task
import wookie.app.cli._
import wookie.http.HttpStream

trait HttpConsumerConf extends Input with URLQuery

abstract class HttpConsumer[A <: HttpConsumerConf](options: Array[String] => A) extends BasicApp[A](options) {

  def createPipeline(opt: A, input: scalaz.stream.Process[Task, Response]): scalaz.stream.Process[Task, Unit]

  def createRequest(opt: A): \/[ParseFailure, Request] = {
    val baseUrl = opt.inputURL()
    val queryParams = opt.query().mapValues(s => Seq(s))
    Uri.fromString(baseUrl).map {
      s => Request(uri = s.setQueryParams(queryParams))
    }
  }
  
  final def run(opt: A): Unit = {
    val http = new AsyncHttpClient
    try {
      val request = createRequest(opt)
      val runResult = for {
        req <- createRequest(opt)
        result <- createPipeline(opt, HttpStream.request(req)(http)).run.attemptRun
      } yield {
        result
      }
      println(s"Processing ended with a result: $runResult")
      if (runResult.isLeft) {
        System.exit(1)
      }
    } finally {
      http.close()
    }
  }
}