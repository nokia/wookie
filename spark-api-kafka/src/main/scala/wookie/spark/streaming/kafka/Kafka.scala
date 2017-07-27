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
package wookie.spark.streaming.kafka

import kafka.serializer.StringDecoder
import kafka.serializer.Decoder
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils
import wookie.{Mappers, Sparkle}
import wookie.spark.DStreams._
import wookie.spark._

import scala.reflect.ClassTag

/**
  * Kafka streams
  */
object Kafka {

  def stream[K: ClassTag, V: ClassTag, KD <: Decoder[K]: ClassTag, VD <: Decoder[V]: ClassTag]
  (brokers: List[String], topics: Set[String]): Sparkle[DStream[(K, V)]] = SparkStreamingRuntime {
    ssc =>
      val brokersList = brokers.map(_.trim).mkString(",")
      val kafkaParams = Map[String, String]("metadata.broker.list" -> brokersList)
      KafkaUtils.createDirectStream[K, V, KD, VD](ssc.get, kafkaParams, topics)
  }

  def textStream(brokers: List[String], topics: Set[String]): Sparkle[DStream[(String, String)]] =
    stream[String, String, StringDecoder, StringDecoder](brokers, topics)

  def typedStream[A: ClassTag, B](brokers: List[String], topic: String,
                                  parser: String => List[A], withId: A => B ): Sparkle[DStream[(B, A)]] = for {
        queueInput <- textStream(brokers, Set(topic))
        typedStream <- Sparkle { _ =>
          queueInput.flatMap( i => parser(i._2) )
        }
        typedStreamWithId <- map(typedStream, Mappers.withFunction(withId))
      } yield {
        typedStreamWithId
      }
}
