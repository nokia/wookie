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
import shapeless.HNil
import wookie.spark.cli.{Checkpoint, SparkStreamingApp, Name, Duration}
import wookie.spark.filters.FilterStream
import wookie.spark.geo.Location
import wookie.spark.mappers.{Maps, MapStream}
import wookie.spark.mappers.Maps._
import wookie.spark.sparkle.StreamingSparkle
import wookie.spark.streaming.twitter.cli.{TwitterConverter, Twitter}
import wookie.spark.streaming.twitter.{TwitterMaps, TwitterStream}
import wookie.spark.streaming.twitter.TwitterFilters.{country, language}

trait UniqueLocationsAppConf extends Twitter with Name with Duration with Checkpoint

case class TweetD(loc: Option[Location])

object UniqueLocations extends SparkStreamingApp[UniqueLocationsAppConf](new ScallopConf(_) with UniqueLocationsAppConf) {

  import TwitterConverter._

  val windowLen = 20L

  override def runStreaming(opt: UniqueLocationsAppConf): Unit = {
    val pipe = for {
      tweets <- TwitterStream(opt)
      onlyUSEnglish <- FilterStream(tweets, country("US"), language("en"))
      onlyLocations <- MapStream(onlyUSEnglish, Maps.From(TwitterMaps.location :: HNil).to[TweetD])
      withKeys <- MapStream(onlyLocations, withId( (a: TweetD) => a.loc))
      onlyUniqueInWindow <- StreamingSparkle {
        withKeys.reduceByKeyAndWindow((a: TweetD, b: TweetD) => a, Minutes(windowLen), Minutes(windowLen))
      }
    } yield {
        onlyUniqueInWindow.map(_._1).saveAsTextFiles("./unique-loc-names", "pps")
    }
    pipe(this)
  }
}

