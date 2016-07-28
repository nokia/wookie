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

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{StreamingContext, StreamingContextState}
import org.junit.runner.RunWith
import org.rogach.scallop.ScallopConf
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

import scala.collection.mutable
import scalaz.concurrent.Task

@RunWith(classOf[JUnitRunner])
class SparkStreamingSpec extends Specification {

  "Should init Spark Streaming object" in {
    var localSc: SparkContext = null
    var localSQL: SparkSession = null
    var localStreaming: StreamingContext = null
    var appName: String = null
    var duration: Long = 0L
    val app = new SparkStreamingApp(new ScallopConf(_) with Name with Duration with Checkpoint) {
      override def runStreaming(opt: ScallopConf with Name with Duration with Checkpoint): Unit = {
        localSc = sc
        localSQL = session
        localStreaming = ssc
        appName = opt.name()
        duration = opt.duration()

        val rdd = sc.parallelize(Seq(100, 200, 300))
        val queue = new mutable.Queue[RDD[Int]]()
        queue.enqueue(rdd)
        ssc.queueStream(queue).print()

      }
    }
    System.setProperty("spark.master", "local")

    val sparkStop = Task.fork(Task.delay {
      val time1 = System.currentTimeMillis()
      while (localStreaming == null || localStreaming.getState() != StreamingContextState.ACTIVE) {
        Thread.sleep(300)
      }
      localStreaming.stop()
    })
    sparkStop.unsafePerformAsync( out => ())
    app.main(Array("--name", "xxx", "--duration", "1000"))
    localSc.stop()
    Option(localSc) must beSome
    Option(localSQL) must beSome
    Option(localStreaming) must beSome
    appName must_== "xxx"
    duration must_== 1000L
  }
}
