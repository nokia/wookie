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
package wookie.collector.streams

import argonaut._
import org.apache.kafka.clients.producer.KafkaProducer
import org.http4s.Request
import org.http4s.client.Client

import scalaz._
import scalaz.concurrent._
import scalaz.stream._
import scalaz.stream.Process._

trait Config {
  def kafkaProducer: KafkaProducer[String, String]
  def httpClient: Client
}

case class JsonDumper[A](dec: DecodeJson[A], enc: EncodeJson[A]) {

  import wookie.app.di.DI._

  def push(req: Request, topics: List[String], kvMap: String => (String, String)): Config => Process[Task, Unit] = config => {
    val pipe = for {
      src <- HttpStream.source(req).local[Config](_.httpClient)
      sink <- KafkaStream.sink[String, String](topics).local[Config](_.kafkaProducer)
    } yield {
      src.flatMap(r => eval(JsonTransformer.asText(r)(dec, enc))).map(kvMap).to(sink)
    }
    pipe.run(config)
  }
}