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
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext}
import org.rogach.scallop.ScallopConf
import wookie.Sparkle
import wookie.cli.{Checkpoint, Duration, Name}
import wookie.spark.SparkStreamingRuntime
import wookie.spark.cli.SparkStreamingApp
import wookie.spark.streaming.kafka.cli.Kafka
import wookie.yql.geo.Location

trait TwitterPopularTagsConf extends Name with Duration with Checkpoint with TwitterConf with Kafka

object PopularTags {
  def stream(tags: DStream[String], windowLenInSeconds: Long): Sparkle[DStream[(Int, String)]] =
    Sparkle { _ =>
    tags.map((_, 1)).window(Seconds(windowLenInSeconds))
      .reduceByKey(_ + _)
      .map{case (topic, count) => (count, topic)}
      .transform(_.sortByKey(false))
  }
}

object TwitterPopularTags extends SparkStreamingApp[TwitterPopularTagsConf](new ScallopConf(_) with TwitterPopularTagsConf) {
  import Twitter._
  import TwitterConverter._
  import wookie.spark.DStreams._
  import wookie.spark.streaming.kafka.Kafka._

  //scalastyle:off
  override def runStreaming(opt: TwitterPopularTagsConf, spark: SparkSession, ssc: StreamingContext): Unit = {
    val pipeline = for {
      tweets <- cleanedTwitterStreamWithLocations(opt, "US", "en", withId=a => a.location)
      hashTags <- flatMap(tweets, (status: (Option[Location], Tweet)) => status._2.tags)
      t1 <- Sparkle(_ => hashTags.map(a => (a, 1)).transform(_.sortByKey(false)))
      topCounts60 <- PopularTags.stream(hashTags, 60)
      topCount60ByTag <- map(topCounts60, (x: (Int, String)) => (x._2, x._1)  )
      weatherStream <- typedStream(opt.brokers(), Weather.queueName, Weather.parse, withId= (w: Weather) => Option(Location(w.area, w.region)))
      joined <- Sparkle { _ =>
        tweets.join(weatherStream.window(Minutes(10)))
      }
    } yield {
        t1.foreachRDD(rdd => {
          rdd.take(1)
          ()
          //println("\nPopular topics in last 60 seconds (%s total):".format(rdd.count()))
          //topList.foreach{ a => println("%s".format(a))}
        })
        //hashTags.print(10)
        joined.foreachRDD(rdd => {
          val topList = rdd.collect()
          println("\n JOINED Tweets with weather (%s total):".format(rdd.count()))
          topList.foreach{case (loc, (tweet, weather)) => println("%s (%s tweets) %s weather".format(loc, tweet.text, weather.conditions))}
        })
      }
    pipeline.run(SparkStreamingRuntime(ssc))
  }
  //scalastyle:on
}
