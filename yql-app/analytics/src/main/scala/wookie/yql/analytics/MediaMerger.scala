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

import org.apache.spark.streaming.Minutes
import org.rogach.scallop.ScallopConf
import wookie.spark.cli._
import wookie.spark.geo.Location
import wookie.spark.mappers.{JoinStreams, SortStreamByKey}
import wookie.spark.streaming.kafka.KafkaTypedStream
import wookie.spark.streaming.kafka.cli.Kafka
import wookie.spark.streaming.twitter.cli.{Twitter, TwitterConverter}
import wookie.spark.streaming.twitter.{LocalizedCleanedTwitterStream, Tweet}

trait MediaMergerConf extends Name with Duration with Twitter with Kafka with Checkpoint

object MediaMergeUtils {
  def defaultLoc: Location = Location("", "USA")
  def asWeatherLocation(w: Weather): Option[Location] = Some(Location(w.area, w.region))
  def asTweetLocation(t: Tweet): Option[Location] = t.location
}

object MediaMerger extends SparkStreamingApp[MediaMergerConf](new ScallopConf(_) with MediaMergerConf) {

  import MediaMergeUtils._
  import TwitterConverter._

  val windowLen = 20L

  def runStreaming(opt: MediaMergerConf): Unit = {
    val pipeline = for {
      tweets <- LocalizedCleanedTwitterStream(opt, "US", "en", withId=asTweetLocation)
      weatherStream <- KafkaTypedStream(opt.brokers(), Weather.queueName, Weather.parse, withId=asWeatherLocation)
      weatherSorted <- SortStreamByKey(weatherStream)
      joined <- JoinStreams(tweets, weatherSorted.window(Minutes(windowLen)))
    } yield {
        joined.print()
    }
    pipeline(this)

  }
}
