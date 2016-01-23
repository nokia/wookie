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
package wookie.collector.cli

import java.util.concurrent.{Future => JFuture}

import argonaut.Argonaut._
import argonaut.{EncodeJson, DecodeJson}
import org.apache.kafka.clients.producer.{RecordMetadata, ProducerRecord, KafkaProducer}
import org.apache.kafka.common.TopicPartition
import org.http4s.{Request, Response}
import org.http4s.client.Client
import org.junit.runner.RunWith
import org.mockito.Matchers
import org.rogach.scallop.ScallopConf
import org.specs2.ScalaCheck
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope
import scodec.bits.ByteVector
import wookie.collector.streams.Config

import scalaz.concurrent.Task

case class TestObj(x: String, y: Int)

/**
  * Created by ljastrze on 11/25/15.
  */
@RunWith(classOf[JUnitRunner])
class KafkaPusherAppSpec extends Specification with ScalaCheck with Mockito {

  val sampleJson = Response(body=scalaz.stream.Process.eval(Task.now(ByteVector("""{ "x": "Alfa", "y": 10 }""".getBytes))))

  "Should push msgs through App" in new context {
    MockedConfig.httpClient.apply(Matchers.any[Request]) returns Task.now(sampleJson)
    val app = new KafkaPusherApp[TestObj](a => MockedConfig)(decoder, encoder)
    val opt = new ScallopConf(Array("--brokers", "Alfa:100", "--input", "http://abc.com", "--topics", "beta")) with AppConf
    opt.afterInit()
    opt.assertVerified()
    app.run(opt)

    there was one(MockedConfig.httpClient).apply(Matchers.any[Request]) andThen one(MockedConfig.kafkaProducer).send(
      Matchers.any[ProducerRecord[String, String]]) andThen
      one(mockedFuture).get andThen one(MockedConfig.kafkaProducer).close andThen one(MockedConfig.httpClient).shutdown()
  }

  trait context extends Scope {
    object MockedConfig extends Config {
      lazy val httpClient = mock[Client]
      lazy val kafkaProducer = mock[KafkaProducer[String, String]]
    }

    private val codec = casecodec2(TestObj.apply, TestObj.unapply)("x", "y")
    val decoder: DecodeJson[TestObj] = codec
    val encoder: EncodeJson[TestObj] = codec
    val mockedFuture = mock[JFuture[RecordMetadata]]

    MockedConfig.httpClient.shutdown() returns Task.now(())
    MockedConfig.kafkaProducer.send(Matchers.any[ProducerRecord[String, String]]) returns mockedFuture
    mockedFuture.get returns new RecordMetadata(new TopicPartition("XXX", 1), 1L, 2L)
  }
}
