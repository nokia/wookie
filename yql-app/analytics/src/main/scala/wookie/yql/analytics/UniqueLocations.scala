package wookie.yql.analytics

import org.apache.spark.streaming.Minutes
import org.rogach.scallop.ScallopConf
import shapeless.HNil
import wookie.spark.cli.{SparkStreamingApp, Name, Duration}
import wookie.spark.filters.FilterStream
import wookie.spark.geo.Location
import wookie.spark.mappers.{Keyer, Maps, MapStream}
import wookie.spark.sparkle.StreamingSparkle
import wookie.spark.streaming.twitter.cli.{TwitterConverter, Twitter}
import wookie.spark.streaming.twitter.{TwitterMaps, TwitterStream}
import wookie.spark.streaming.twitter.TwitterFilters.{country, language}

trait UniqueLocationsAppConf extends Twitter with Name with Duration

case class TweetD(loc: Option[Location])

object UniqueLocations extends SparkStreamingApp[UniqueLocationsAppConf](new ScallopConf(_) with UniqueLocationsAppConf) {

  import TwitterConverter._

  override def runStreaming(opt: UniqueLocationsAppConf): Unit = {
    val pipe = for {
      tweets <- TwitterStream(opt)
      onlyUSEnglish <- FilterStream(tweets, country("US"), language("en"))
      onlyLocations <- MapStream(onlyUSEnglish, Maps.from(TwitterMaps.location :: HNil).to[TweetD])
      withKeys <- MapStream(onlyLocations, Keyer.withId( (a: TweetD) => a.loc))
      onlyUniqueInWindow <- StreamingSparkle {
        withKeys.reduceByKeyAndWindow((a: TweetD, b: TweetD) => a, Minutes(20), Minutes(20))
      }
    } yield {
        onlyUniqueInWindow.map(_._1).saveAsTextFiles("/Users/lukaszjastrzebski/Projects/dasp/wookie-data/unique-loc-names", "pps")
    }
    pipe(this)
  }
}

