package wookie.yql.analytics

import com.javadocmd.simplelatlng.LatLng
import org.apache.spark.streaming.{Seconds, Minutes}
import org.rogach.scallop.ScallopConf
import shapeless._
import wookie.spark.cli._
import wookie.spark.filters.FilterStream
import wookie.spark.geo.Location
import wookie.spark.mappers.{Keyer, MapStream}
import wookie.spark.mappers.Maps.from
import wookie.spark.sparkle.StreamingSparkle
import wookie.spark.streaming.kafka.KafkaTypedStream
import wookie.spark.streaming.kafka.cli.Kafka
import wookie.spark.streaming.twitter.TwitterStream
import wookie.spark.streaming.twitter.TwitterFilters.{country, language}
import wookie.spark.streaming.twitter.TwitterMaps._
import wookie.spark.streaming.twitter.cli.{TwitterConverter, Twitter}

case class Tweet(user: String, refUsers: List[String], refUrls: List[String], tags: List[String],
                 location: Option[Location], latLong: Option[LatLng], text: String)

trait MediaMergerConf extends Name with Duration with Twitter with Kafka

object MediaMergeUtils {
  val countryCode = "US"
  def notEmptyTweet: Tweet => Boolean = t => !t.text.isEmpty
  def extractors = user :: refUsers :: urls :: tags :: location :: latLong :: text :: HNil
  def onlyUS = country(countryCode)
  def english = language("en")
  def defaultLoc = Location("", "USA")
  def byWeatherRegion(w: Weather) = w.region
  def byTweetRegion(t: Tweet) = t.location.getOrElse(defaultLoc).region
}

object MediaMerger extends SparkStreamingApp[MediaMergerConf](new ScallopConf(_) with MediaMergerConf) {

  import MediaMergeUtils._
  import TwitterConverter._
  
  def runStreaming(opt: MediaMergerConf): Unit = {
    val pipeline = for {
      tweets <- TwitterStream(opt)
      onlyUSEnglish <- FilterStream(tweets, onlyUS, english)
      cleanTweets <- MapStream(onlyUSEnglish, from(extractors).to[Tweet])
      notEmptyCleanTweets <- FilterStream(cleanTweets, notEmptyTweet)
      weatherStream <- KafkaTypedStream[Weather](opt.brokers(), Weather.queueName, Weather.parse)
      weatherWithId <- MapStream(weatherStream, Keyer.withId(byWeatherRegion))
      tweetsWithId <- MapStream(notEmptyCleanTweets, Keyer.withId(byTweetRegion))
      joined <- StreamingSparkle {
        tweetsWithId.fullOuterJoin(weatherWithId.window(Seconds(opt.duration()/1000)))
      }
    } yield {
      joined.print()
    }
    pipeline(this)

  }
}