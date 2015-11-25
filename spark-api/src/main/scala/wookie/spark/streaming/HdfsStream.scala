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
package wookie.spark.streaming

import wookie.spark.cli.SparkStreamingApp

import scala.reflect.ClassTag
import org.apache.hadoop.mapreduce.InputFormat
import org.apache.spark.streaming.dstream.InputDStream
import wookie.spark.sparkle.Sparkle
import org.apache.spark.streaming.dstream.DStream

sealed trait HdfsStream[K] extends Sparkle[K, SparkStreamingApp[_]]

case class HdfsCustomStream[K: ClassTag, V: ClassTag, F <: InputFormat[K, V]: ClassTag](url: String) extends HdfsStream[DStream[(K, V)]] {
  def apply(app: SparkStreamingApp[_]): DStream[(K, V)] = {
    app.ssc.fileStream[K, V, F](url)
  }
}

case class HdfsTextStream(url: String) extends HdfsStream[DStream[String]] {
  def apply(app: SparkStreamingApp[_]): DStream[String] = {
    app.ssc.textFileStream(url)
  }
}
