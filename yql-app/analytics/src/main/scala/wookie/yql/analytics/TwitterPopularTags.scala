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
package wookie.yql.analytics

import org.apache.spark.Logging
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Minutes, Seconds}
import org.rogach.scallop.ScallopConf
import wookie.spark.cli.{Checkpoint, Duration, Name, SparkStreamingApp}
import wookie.spark.geo.Location
import wookie.spark.mappers.FlatMapStream
import wookie.spark.sparkle.{Sparkle, StreamingSparkle}
import wookie.spark.streaming.kafka.KafkaTypedStream
import wookie.spark.streaming.kafka.cli.Kafka
import wookie.spark.streaming.twitter.cli.{Twitter, TwitterConverter}
import wookie.spark.streaming.twitter.{LocalizedCleanedTwitterStream, Tweet}

trait TwitterPopularTagsConf extends Name with Duration with Checkpoint with Twitter with Kafka

case class PopularTags(tags: DStream[String], windowLenInSeconds: Long) extends Sparkle[DStream[(Int, String)], SparkStreamingApp[_]] {

  def apply(app: SparkStreamingApp[_]): DStream[(Int, String)] = {
    tags.map((_, 1)).window(Seconds(windowLenInSeconds))
      .reduceByKey(_ + _)
      .map{case (topic, count) => (count, topic)}
      .transform(_.sortByKey(false))
  }
}

object TwitterPopularTags extends SparkStreamingApp[TwitterPopularTagsConf](new ScallopConf(_) with TwitterPopularTagsConf) with Logging {
  import TwitterConverter._

  val windowLen = 10L

  override def runStreaming(opt: TwitterPopularTagsConf): Unit = {
    val pipeline = for {
      tweets <- LocalizedCleanedTwitterStream(opt, "US", "en", withId=a => a.location)
      hashTags <- FlatMapStream(tweets, (status: (Option[Location], Tweet)) => status._2.tags)
      t1 <- StreamingSparkle(hashTags.map(a => (a, 1)).transform(_.sortByKey(ascending = false)))
      //topCounts60 <- PopularTags(hashTags, 60)
      //topCount60ByTag <- MapStream(topCounts60, (x: (Int, String)) => (x._2, x._1)  )
      weatherStream <- KafkaTypedStream(opt.brokers(), Weather.queueName, Weather.parse, withId= (w: Weather) => Option(Location(w.area, w.region)))
      joined <- StreamingSparkle {
        tweets.join(weatherStream.window(Minutes(windowLen)))
      }
    } yield {
        t1.foreachRDD(rdd => {
          rdd.take(1)
          ()
          //logInfo("\nPopular topics in last 60 seconds (%s total):".format(rdd.count()))
          //topList.foreach{ a => logInfo("%s".format(a))}
        })
        //hashTags.print(10)
        joined.foreachRDD(rdd => {
          val topList = rdd.collect()
          logInfo("\n JOINED Tweets with weather (%s total):".format(rdd.count()))
          topList.foreach{case (loc, (tweet, weather)) => logInfo("%s (%s tweets) %s weather".format(loc, tweet.text, weather.conditions))}
        })
      }
    pipeline(this)
  }
}
