package wookie.yql.analytics

import org.apache.spark.streaming.dstream.DStream
import org.rogach.scallop.ScallopConf
import shapeless._
import twitter4j.Status
import wookie.spark.SparkStreamingApp
import wookie.spark.cli._
import wookie.spark.filters.FilterBy
import wookie.spark.mappers._
import wookie.spark.sparkle.streaming.TwitterFilters._
import wookie.spark.sparkle.streaming.{KafkaTypedStream, TwitterStream}

import scala.collection.immutable.{:: => Cons}

trait WeatherConf extends Name with Duration with Kafka

object WeatherClustering extends SparkStreamingApp[WeatherConf](new ScallopConf(_) with WeatherConf) {

  def runStreaming(opt: WeatherConf): Unit = {
    
  }
  def run(opt: WeatherConf, stream: DStream[(String, String)]): Unit = {
    stream.print()
  }
}

trait MediaMergerConf extends Name with Duration with Twitter with Kafka

case class User(user: String)

object MediaMerger extends SparkStreamingApp[MediaMergerConf](new ScallopConf(_) with MediaMergerConf) {

  val countryCode = "US"
  val whitelist = "abcdefghijklmnopqrstuvwxyz 1234567890".toSet
  import ScallopConfsConverter._
  
  def runStreaming(opt: MediaMergerConf): Unit = {
    val mappers = extractUser :: HNil
    val pipeline = for {
      tweets <- TwitterStream(opt)
      onlyUSEnglish <- FilterBy(tweets, country(countryCode), language("en"))

      cleanupedTweets <- dstream(onlyUSEnglish, mappers).map[User](the[Traversal.Traverser[Status, shapeless.::[Status => String, HNil]]])
      weatherStream <- KafkaTypedStream[Weather](opt.brokers(), "weather", Weather.parse)
//      joined <- new JoinStreamInWindow(onlyUS, weather, Minutes(60))
    } yield {
      cleanupedTweets.print()
      weatherStream.print()
    }
    pipeline(this)
    ()
  }
}