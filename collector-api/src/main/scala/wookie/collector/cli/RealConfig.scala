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

import org.apache.kafka.clients.producer.KafkaProducer
import org.http4s.client.Client
import wookie.collector.streams.Config

import scala.collection.JavaConverters._

case class RealConfig(brokers: List[String]) extends Config {

  val kafkaProducer: KafkaProducer[String, String] = {
    new KafkaProducer[String, String](Map[String, Object](
      "bootstrap.servers" -> brokers.mkString(","),
      "value.serializer" -> "org.apache.kafka.common.serialization.StringSerializer",
      "acks" -> "1",
      "timeout.ms" -> "5000",
      "metadata.fetch.timeout.ms" -> "5000",
      "retries" -> "0",
      "key.serializer" -> "org.apache.kafka.common.serialization.StringSerializer").asJava)
  }

  val httpClient: Client = org.http4s.client.blaze.defaultClient
}
