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
package wookie.yql.analytics

import org.apache.spark.sql.SparkSession
import org.rogach.scallop.ScallopConf
import org.apache.spark.streaming.Durations._
import org.apache.spark.streaming.StreamingContext
import wookie.cli.{Checkpoint, Duration, Name}
import wookie.spark.cli._
import wookie.yql.geo.Location
import wookie.spark.mappers.DStreams._
import wookie.spark.streaming.kafka.Kafka._
import wookie.spark.streaming.kafka.cli.Kafka

trait MediaMergerConf extends Name with Duration with TwitterConf with Kafka with Checkpoint


object MediaMerger extends SparkStreamingApp[MediaMergerConf](new ScallopConf(_) with MediaMergerConf) {

  import Twitter.cleanedTwitterStreamWithLocations
  import TwitterConverter._

  private def defaultLoc = Location("", "USA")
  private def asWeatherLocation(w: Weather): Option[Location] = Some(Location(w.area, w.region))
  private def asTweetLocation(t: Tweet) = t.location

  def runStreaming(opt: MediaMergerConf, spark: SparkSession, ssc: StreamingContext): Unit = {
    val pipeline = for {
      tweets <- cleanedTwitterStreamWithLocations(opt, "US", "en", withId=asTweetLocation)
      weatherStream <- typedStream(opt.brokers(), Weather.queueName, Weather.parse, withId=asWeatherLocation)
      weatherSorted <- sortByKey(weatherStream)
      joined <- join(tweets, weatherSorted.window(minutes(20)))
    } yield {
      joined.print()
    }
    pipeline.run(ssc)

  }
}
