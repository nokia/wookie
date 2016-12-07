/*
 * Copyright (C) 2014-2015 by Nokia.
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
 *
 */
package wookie.collector.streams

import java.util.concurrent.{Future => JFuture}

import argonaut._
import Argonaut._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.TopicPartition
import org.http4s.{MalformedMessageBodyFailure, Request, Response, Uri}
import org.junit.runner.RunWith
import org.mockito.Matchers
import org.specs2.ScalaCheck
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope
import scodec.bits.ByteVector
import wookie.collector.MockedClient

import scalaz.concurrent.Task


case class TestObj(x: String, y: Int)

@RunWith(classOf[JUnitRunner])
class JsonDumperSpec extends Specification with ScalaCheck with Mockito {

  val sampleJson = Response(body = scalaz.stream.Process.eval(Task.now(ByteVector("""{ "x": "Alfa", "y": 10 }""".getBytes))))
  val incorrectJson = Response(body = scalaz.stream.Process.eval(Task.now(ByteVector(""""x": "Alfa", "y": 10""".getBytes))))

  "Should dump json objects" in new context {
    val config = createConfig(sampleJson)
    kafkaSetup(config)

    val dumper = JsonDumper(decoder, encoder)
    val pipe = dumper.push(req, List("a"), (a: String) => (a, a))
    val result = pipe(config).run.unsafePerformSyncAttempt

    result.isRight must_== true
    there was one(config.kafkaProducer).
      send(Matchers.any[ProducerRecord[String, String]]) andThen one(mockedFuture).get andThen one(config.kafkaProducer).close
  }

  "Should not dump incorrect json objects" in new context {
    val config = createConfig(incorrectJson)
    kafkaSetup(config)

    val dumper = JsonDumper(decoder, encoder)
    val pipe = dumper.push(req, List("a"), (a: String) => (a, a))
    val result = pipe(config).run.unsafePerformSyncAttempt

    result.isLeft must_== true
    result.toEither.left.get.getClass must_== classOf[MalformedMessageBodyFailure]
    there was no(config.kafkaProducer).send(Matchers.any[ProducerRecord[String, String]])
    there was no(config.kafkaProducer).close
  }

  trait context extends Scope {

    def createConfig(response: Response): Config = new Config {
      lazy val httpClient = MockedClient.create(response)
      lazy val kafkaProducer = mock[KafkaProducer[String, String]]
    }

    private val codec = casecodec2(TestObj.apply, TestObj.unapply)("x", "y")
    val decoder: DecodeJson[TestObj] = codec
    val encoder: EncodeJson[TestObj] = codec

  }

  val mockedFuture = mock[JFuture[RecordMetadata]]

  def kafkaSetup(conf: Config): Unit = {
    conf.kafkaProducer.send(Matchers.any[ProducerRecord[String, String]]) returns mockedFuture
    mockedFuture.get returns new RecordMetadata(new TopicPartition("XXX", 1), 1L, 2L)
    ()
  }

  val req = Request(uri = Uri.uri("http://xxx.com/v1/2"))

}
