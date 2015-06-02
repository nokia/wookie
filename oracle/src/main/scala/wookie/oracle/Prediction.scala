package wookie.oracle

import org.http4s.server._
import org.http4s.dsl._

object Prediction {

  def service = HttpService {
    case GET -> Root / "ping" =>
      // EntityEncoder allows for easy conversion of types to a response body
      Ok("pong")
  }

}