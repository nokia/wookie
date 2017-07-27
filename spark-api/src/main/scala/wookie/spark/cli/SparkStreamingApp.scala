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
package wookie.spark.cli

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{SQLImplicits, SparkSession}
import org.apache.spark.streaming.{Milliseconds, StreamingContext}
import wookie.cli.{Checkpoint, Duration, Name}

/**
  * Spark streaming application
  *
  * @param options function that will create parsed arguments of type A
  * @tparam A type of cmd line arguments, at least name of application needs to be passed
  */
abstract class SparkStreamingApp[A <: Name with Duration with Checkpoint](options: Array[String] => A) extends SparkApp[A](options) {

  private def createStreamingContext(opt: A, spark: SparkSession): () => StreamingContext = {
    () =>
      val ssc = new StreamingContext(spark.sparkContext, Milliseconds(opt.duration()))
      setStreamingLogLevels()
      runStreaming(opt, spark, ssc)
      opt.checkpointDir.get.foreach { chkPoint =>
        ssc.checkpoint(chkPoint)
      }
      ssc
  }

  def runStreaming(opt: A, spark: SparkSession, ssc: StreamingContext): Unit

  final def run(opt: A, spark: SparkSession): Unit = {
    val ssc = if (opt.checkpointDir.get.isDefined) {
      StreamingContext.getOrCreate(opt.checkpointDir(), createStreamingContext(opt, spark), createOnError = true)
    } else {
      createStreamingContext(opt, spark)()
    }
    ssc.start()
    ssc.awaitTermination()
  }

  def setStreamingLogLevels(): Unit = {
    val log4jInitialized = Logger.getRootLogger.getAllAppenders.hasMoreElements
    if (!log4jInitialized) {
      Logger.getRootLogger.setLevel(Level.WARN)
    }
  }
}
