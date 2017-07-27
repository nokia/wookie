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
package wookie.spark.mappers

import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.Time
import org.apache.spark.streaming.dstream.DStream
import wookie.{Bools, Containers}
import wookie.spark.StreamingSparkle

import scala.reflect.ClassTag

object DStreams extends Containers[StreamingSparkle, DStream] {

  def fullJoin[A: ClassTag, B: ClassTag, C: ClassTag](stream1: DStream[(A, B)], stream2: DStream[(A, C)]):
  StreamingSparkle[DStream[(A, (Option[B], Option[C]))]] = StreamingSparkle {
    _ => stream1.fullOuterJoin(stream2)
  }

  def leftJoin[A: ClassTag, B: ClassTag, C: ClassTag](stream1: DStream[(A, B)], stream2: DStream[(A, C)]):
  StreamingSparkle[DStream[(A, (B, Option[C]))]] = StreamingSparkle {
    _ => stream1.leftOuterJoin(stream2)
  }

  def join[A: ClassTag, B: ClassTag, C: ClassTag](stream1: DStream[(A, B)], stream2: DStream[(A, C)]):
  StreamingSparkle[DStream[(A, (B, C))]] = StreamingSparkle {
    _ => stream1.join(stream2)
  }

  def sortByKey[A: ClassTag, B: ClassTag](stream: DStream[(A, B)], ascending: Boolean = true)
                                         (implicit ord: Ordering[A]): StreamingSparkle[DStream[(A, B)]] =
    StreamingSparkle {
      _ => stream.transform(_.sortByKey(ascending))
  }

  def flatMap[A, B: ClassTag](@transient stream: DStream[A], func: A => Traversable[B]):
  StreamingSparkle[DStream[B]] = StreamingSparkle {
    _ => stream.flatMap { value =>
      func(value)
    }
  }

  def map[A, B: ClassTag](@transient stream: DStream[A], func: A => B): StreamingSparkle[DStream[B]] = StreamingSparkle {
    _ => stream.map { value =>
      func(value)
    }
  }

  def filter[A](stream: DStream[A], filter: A => Boolean, moreFilters: (A => Boolean) *):
  StreamingSparkle[DStream[A]] = StreamingSparkle { _ =>
    stream.filter(Bools.and(filter, moreFilters: _*))
  }

  def transform[A: ClassTag, B: ClassTag](stream: DStream[A], transformFunc: RDD[A] => RDD[B]):
  StreamingSparkle[DStream[B]] = StreamingSparkle {
    _ => stream.transform(transformFunc)
  }

  def transformWithTime[A: ClassTag, B: ClassTag](stream: DStream[A], transformFunc: (RDD[A], Time) => RDD[B]):
  StreamingSparkle[DStream[B]] = StreamingSparkle {
    _ => stream.transform(transformFunc)
  }

}
