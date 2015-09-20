package wookie.spark.streaming.twitter

import com.javadocmd.simplelatlng.LatLng
import org.apache.spark.streaming.dstream.DStream
import shapeless._
import wookie.spark.cli.SparkStreamingApp
import wookie.spark.filters.FilterStream
import wookie.spark.geo.Location
import wookie.spark.mappers.{Maps, MapStream}
import wookie.spark.mappers.Maps.from
import wookie.spark.sparkle.Sparkle
import wookie.spark.streaming.twitter.TwitterFilters._
import wookie.spark.streaming.twitter.TwitterMaps._

case class Tweet(user: String, refUsers: List[String], refUrls: List[String], tags: List[String],
                 location: Option[Location], latLong: Option[LatLng], text: String)

case class LocalizedCleanedTwitterStream[B](credentials: Credentials, countryCode: String, languageCode: String,
                                         filters: Option[List[String]] = None, withId: Tweet => B)  extends Sparkle[DStream[(B, Tweet)], SparkStreamingApp[_]] {

  def extractors = user :: refUsers :: urls :: tags :: location :: latLong :: text :: HNil
  def notEmptyTweet: Tweet => Boolean = t => !t.text.isEmpty

  def apply(app: SparkStreamingApp[_]): DStream[(B, Tweet)] = {
    val pipeline = for {
      tweets <- TwitterStream(credentials, filters)
      onlyUSEnglish <- FilterStream(tweets, country(countryCode), language(languageCode))
      cleanTweets <- MapStream(onlyUSEnglish, from(extractors).to[Tweet])
      notEmptyCleanTweets <- FilterStream(cleanTweets, notEmptyTweet)
      withId <- MapStream(notEmptyCleanTweets, Maps.withId(withId))
    } yield {
        withId
    }
    pipeline(app)
  }

}
