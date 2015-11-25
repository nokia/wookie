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
package wookie.spark.cli

import org.apache.log4j.{Level, Logger}
import org.apache.spark.streaming.{Milliseconds, StreamingContext}

abstract class SparkStreamingApp[A <: Name with Duration with Checkpoint](options: Array[String] => A) extends SparkApp[A](options) {

  protected var _ssc: StreamingContext = _
  def ssc: StreamingContext = _ssc

  private def createStreamingContext(opt: A): () => StreamingContext = {
    () =>
      _ssc = new StreamingContext(sc, Milliseconds(opt.duration()))
      setStreamingLogLevels()
      runStreaming(opt)
      opt.checkpointDir.get.map { chkPoint =>
        _ssc.checkpoint(chkPoint)
      }
      _ssc
  }

  def runStreaming(opt: A): Unit

  final def run(opt: A): Unit = {
    if (opt.checkpointDir.get.isDefined) {
      _ssc = StreamingContext.getOrCreate(opt.checkpointDir(), createStreamingContext(opt), createOnError = true)
    } else {
      _ssc = createStreamingContext(opt)()
    }
    _ssc.start()
    _ssc.awaitTermination()
  }

  def setStreamingLogLevels(): Unit = {
    val log4jInitialized = Logger.getRootLogger.getAllAppenders.hasMoreElements
    if (!log4jInitialized) {
      Logger.getRootLogger.setLevel(Level.WARN)
    }
  }
}