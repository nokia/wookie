package wookie.http

import org.http4s.EntityDecoder
import org.http4s.Request
import com.ning.http.client.AsyncCompletionHandler
import com.ning.http.client.AsyncHttpClient
import com.ning.http.client.RequestBuilder
import com.ning.http.client.Response
import com.ning.http.client.uri.Uri
import scalaz._
import argonaut._, Argonaut._
import scalaz.concurrent._
import scalaz.stream._
import scalaz.stream.Process._
import scalaz.stream.io._
import scalaz.stream.process1._
import argonaut.DecodeJson

case class StatusException(val code: Int = 0, val message: String) extends Exception(message)

object HttpStream {

  type HttpReader[A] = Reader[AsyncHttpClient, A]

  def singleResource[F[_], R, O](acquire: F[R])(
    release: R => F[Unit])(
      step: R => F[O]): Process[F, O] =
    eval(acquire).flatMap { r =>
      eval(step(r)).onComplete(eval_(release(r)))
    }

  def request(request: Request): HttpReader[Process[Task, Response]] = Reader { httpClient =>
    val acquire = Task.delay(httpClient)
    singleResource(acquire)(src => Task.delay(())) { src =>
      Task.async {
        val req = src.prepareRequest(transform(request))
        (k: (Throwable \/ Response) => Unit) => req.execute(asyncHandler(k)); ()
      }
    }
  }

  def transformJson[A](implicit decoder: DecodeJson[A], encoder: EncodeJson[A]): Process1[String, String] = {
    lift(src => convert(src)(decoder, encoder))
  }

  def convert[A](response: String)(implicit decoder: DecodeJson[A], encoder: EncodeJson[A]): String = {
    response.decodeEither[A] match {
      case \/-(a) =>
        val result = a.asJson.nospaces
        result
      case -\/(msg) =>
        throw new StatusException(message = msg)
    }
  }

  private[this] def asyncHandler(k: (Throwable \/ Response) => Unit) = new AsyncCompletionHandler[Unit] {
    override def onCompleted(r: Response): Unit = r.getStatusCode match {
      case 200 | 201 | 202 | 203 | 204 | 205 | 206 => k(\/-(r))
      case _                                       => k(-\/(new StatusException(r.getStatusCode, r.getStatusText)))
    }
    override def onThrowable(t: Throwable): Unit = k(-\/(t))
  }

  private[this] def transform(request: Request): com.ning.http.client.Request = {
    val b = new RequestBuilder(request.method.name)
    val ruri = request.uri
    b.setUri(Uri.create(ruri.renderString))

    request.headers.toList.foreach {
      x => b.addHeader(x.name.value, x.value)
    }

    val body = EntityDecoder.decodeString(request).run
    b.setBody(body)

    b.build()
  }
}