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

import org.apache.kafka.clients.producer.{ProducerRecord, KafkaProducer}

import scalaz.\/
import scalaz.concurrent.{Future, Task}
import scalaz.stream._
import scalaz.stream.io._

object KafkaStream {

  private def jFutureToTask[T](fut: JFuture[T]): Task[T] = {
    val fu = Future {
      \/.fromTryCatchNonFatal(fut.get)
    }
    new Task(fu)
  }

  def sink[K, V](topics: List[String]): KafkaProducer[K, V] => Sink[Task, (K, V)] = producer => {

    def push(producer: KafkaProducer[K, V], topics: List[String], id: K, msg: V): Task[Unit] = {
      val tasks = for {
        topic <- topics
      } yield {
        val record = new ProducerRecord(topic, id, msg)
        val future = producer.send(record)
        jFutureToTask(future.asInstanceOf[JFuture[Unit]])
      }
      Task.gatherUnordered(tasks, true).map( r => ())
    }

    resource(Task.delay(producer))(os => Task.delay(os.close))(
      os => Task.now((bytes: (K, V)) => push(producer, topics, bytes._1, bytes._2)))
  }
}
