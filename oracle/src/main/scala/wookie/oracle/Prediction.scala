package wookie.oracle

import org.http4s.server._
import org.http4s.dsl._
import org.apache.spark.mllib.classification.LogisticRegressionModel
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.Vectors

object Prediction {

  val sc = new SparkContext()
  def service = HttpService {
    case GET -> Root / "ping" =>
      // EntityEncoder allows for easy conversion of types to a response body
      val model  = LogisticRegressionModel.load(sc, "/Users/lukaszjastrzebski/Projects/LGModel")
      val result = model.predict(Vectors.dense(0.0, 1.1, 0.1))
      Ok(s"pong: $result")
  }

}