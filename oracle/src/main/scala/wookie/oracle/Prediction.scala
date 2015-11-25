/* Copyright (C) 2014-2015 by Nokia.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package wookie.oracle

import org.http4s.server._
import org.http4s.dsl._
import org.apache.spark.mllib.classification.LogisticRegressionModel
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.Vectors

object Prediction {

  val sc = new SparkContext()
  def service: HttpService = HttpService {
    case GET -> Root / "ping" =>
      // EntityEncoder allows for easy conversion of types to a response body
      val model  = LogisticRegressionModel.load(sc, "/Users/lukaszjastrzebski/Projects/LGModel")
      val result = model.predict(Vectors.dense(0.0, 1.1, 0.1))
      Ok(s"pong: $result")
  }

}