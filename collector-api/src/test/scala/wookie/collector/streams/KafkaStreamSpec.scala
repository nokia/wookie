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

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

import scala.collection.JavaConverters._

import org.apache.kafka.clients.producer.{RecordMetadata, ProducerRecord, KafkaProducer}
import org.apache.kafka.common.TopicPartition
import org.mockito.ArgumentCaptor
import org.scalacheck.{Gen, Arbitrary}
import org.specs2.ScalaCheck
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scalaz.stream._

/**
  * Created by ljastrze on 11/24/15.
  */
@RunWith(classOf[JUnitRunner])
class KafkaStreamSpec extends Specification with ScalaCheck with Mockito {

  "Should push as many kafka msgs as topics" >> prop { (topics: List[String], key: String, value: String) =>
    val producer = mock[KafkaProducer[String, String]]
    val mockedFuture = mock[JFuture[RecordMetadata]]
    mockedFuture.get() returns new RecordMetadata(new TopicPartition("SomeDummyName", 1), 1, 2)
    val captor = ArgumentCaptor.forClass(classOf[ProducerRecord[String, String]])
    producer.send(any[ProducerRecord[String, String]]) returns mockedFuture

    val p1 = Process.constant((key,value)).toSource
    val sink = KafkaStream.sink(topics)(producer)
    val result = p1.to(sink).take(1).run.attemptRun

    there was topics.length.times(producer).send(captor.capture()) andThen one(producer).close
    val capturedValues = captor.getAllValues.asScala.toList

    val allMatchers = topics.zipWithIndex.map { case (topic, idx) =>
      val record = capturedValues(idx)
      (record.key() must_== key) and (record.value() must_== value) and (record.topic() must_== topic)
    }
    allMatchers
  }.setArbitrary1(Arbitrary(Gen.containerOf[List, String](Gen.identifier))).setArbitrary2(Arbitrary(Gen.identifier))


}
