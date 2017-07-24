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
import org.apache.spark.streaming.{Minutes, StreamingContext}
import org.rogach.scallop.ScallopConf
import shapeless.HNil
import wookie.spark.cli._
import wookie.spark.mappers.Mappers.From
import wookie.spark.sparkle.StreamingSparkle
import wookie.yql.geo.Location

trait UniqueLocationsAppConf extends TwitterConf with Name with Duration with Checkpoint with Output

case class TweetD(loc: Option[Location])

object UniqueLocations extends SparkStreamingApp[UniqueLocationsAppConf](new ScallopConf(_) with UniqueLocationsAppConf) {

  import TwitterConverter._
  import Twitter._
  import wookie.spark.mappers.StreamMappers._
  import wookie.spark.filters.Filters._
  import wookie.spark.mappers.Mappers.withId


  override def runStreaming(opt: UniqueLocationsAppConf, spark: SparkSession, ssc: StreamingContext): Unit = {
    val pipe = for {
      tweets <- twitterStream(opt)
      onlyUSEnglish <- filterStream(tweets, country("US"), language("en"))
      onlyLocations <- map(onlyUSEnglish, From(location :: HNil).to[TweetD])
      withKeys <- map(onlyLocations, withId( (a: TweetD) => a.loc))
      onlyUniqueInWindow <- StreamingSparkle { _ =>
        withKeys.reduceByKeyAndWindow((a: TweetD, b: TweetD) => a, Minutes(20), Minutes(20))
      }
    } yield {
        onlyUniqueInWindow.map(_._1).saveAsTextFiles(opt.outputURL(), "pps")
    }
    pipe.run(ssc)
  }
}
