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
package wookie.spark.streaming

import org.apache.hadoop.mapreduce.InputFormat
import org.apache.spark.streaming.dstream.DStream
import wookie.spark.StreamingSparkle

import scala.reflect.ClassTag

/**
  * FileStreams
  */
object FileStreams {

  def file[K: ClassTag, V: ClassTag, F <: InputFormat[K, V] : ClassTag](url: String):
  StreamingSparkle[DStream[(K, V)]] = StreamingSparkle { ssc =>
    ssc.fileStream[K, V, F](url)
  }

  def text(url: String): StreamingSparkle[DStream[String]] = StreamingSparkle { ssc =>
    ssc.textFileStream(url)
  }
}
