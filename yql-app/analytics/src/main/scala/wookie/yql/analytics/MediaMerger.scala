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
  def defaultLoc = Location("", "USA")
  def asWeatherLocation(w: Weather): Option[Location] = Some(Location(w.area, w.region))
  def asTweetLocation(t: Tweet) = t.location
}

object MediaMerger extends SparkStreamingApp[MediaMergerConf](new ScallopConf(_) with MediaMergerConf) {

  import MediaMergeUtils._
  import TwitterConverter._

  def runStreaming(opt: MediaMergerConf): Unit = {
    val pipeline = for {
      tweets <- LocalizedCleanedTwitterStream(opt, "US", "en", withId=asTweetLocation)
      weatherStream <- KafkaTypedStream(opt.brokers(), Weather.queueName, Weather.parse, withId=asWeatherLocation)
      weatherSorted <- SortStreamByKey(weatherStream)
      joined <- JoinStreams(tweets, weatherSorted.window(Minutes(20)))
    } yield {
        joined.print()
    }
    pipeline(this)

  }
}
