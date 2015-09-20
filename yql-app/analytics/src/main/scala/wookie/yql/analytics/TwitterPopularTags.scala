package wookie.yql.analytics

import org.apache.spark.streaming.{Minutes, Seconds}
import org.apache.spark.streaming.dstream.DStream
import org.rogach.scallop.ScallopConf
import twitter4j.Status
import wookie.spark.cli.{Name, Duration, Checkpoint, SparkStreamingApp}
import wookie.spark.geo.Location
import wookie.spark.mappers.{MapStream, FlatMapStream}
import wookie.spark.sparkle.{StreamingSparkle, Sparkle}
import wookie.spark.streaming.kafka.KafkaTypedStream
import wookie.spark.streaming.kafka.cli.Kafka
import wookie.spark.streaming.twitter.{Tweet, LocalizedCleanedTwitterStream, TwitterStream}
import wookie.spark.streaming.twitter.cli.{TwitterConverter, Twitter}

trait TwitterPopularTagsConf extends Name with Duration with Checkpoint with Twitter with Kafka

case class PopularTags(tags: DStream[String], windowLenInSeconds: Long) extends Sparkle[DStream[(Int, String)], SparkStreamingApp[_]] {

  def apply(app: SparkStreamingApp[_]): DStream[(Int, String)] = {
    tags.map((_, 1)).window(Seconds(windowLenInSeconds))
      .reduceByKey(_ + _)
      .map{case (topic, count) => (count, topic)}
      .transform(_.sortByKey(false))
  }
}

object TwitterPopularTags extends SparkStreamingApp[TwitterPopularTagsConf](new ScallopConf(_) with TwitterPopularTagsConf) {
  import TwitterConverter._

  override def runStreaming(opt: TwitterPopularTagsConf): Unit = {
    val pipeline = for {
      tweets <- LocalizedCleanedTwitterStream(opt, "US", "en", withId=a => a.location)
      hashTags <- FlatMapStream(tweets, (status: (Option[Location], Tweet)) => status._2.tags)
      t1 <- StreamingSparkle(hashTags.map(a => (a, 1)).transform(_.sortByKey(false)))
      //topCounts60 <- PopularTags(hashTags, 60)
      //topCount60ByTag <- MapStream(topCounts60, (x: (Int, String)) => (x._2, x._1)  )
      weatherStream <- KafkaTypedStream(opt.brokers(), Weather.queueName, Weather.parse, withId= (w: Weather) => Option(Location(w.area, w.region)))
      joined <- StreamingSparkle {
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
    pipeline(this)
  }
}
