package wookie.spark.sparkle.streaming

import scala.math.pow
import scalaz._
import scalaz.std.anyVal._
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.twitter.TwitterUtils
import twitter4j.Status
import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder
import wookie.spark.SparkStreamingApp
import wookie.spark.sparkle.Sparkle
import wookie.spark.sparkle.Sparkles


case class Credentials(consumerKey: String, consumerSecret: String, accessToken: String, accessTokenSecret: String)

case class TwitterStream(credentials: Credentials, filters: Option[List[String]] = None) extends Sparkle[DStream[Status], SparkStreamingApp[_]] {
  def run(app: SparkStreamingApp[_]): DStream[Status] = {
    val authorization = new OAuthAuthorization(new ConfigurationBuilder().
      setOAuthConsumerKey(credentials.consumerKey).
      setOAuthConsumerSecret(credentials.consumerSecret).
      setOAuthAccessToken(credentials.accessToken).
      setOAuthAccessTokenSecret(credentials.accessTokenSecret)
      build ())
    TwitterUtils.createStream(app.ssc, Some(authorization), filters.getOrElse(Nil))
  }

}

case class FilterTweetsBy(stream: DStream[Status], filter: Status => Boolean, moreFilters: Status => Boolean *) extends Sparkle[DStream[Status], SparkStreamingApp[_]] {
  
  def run(app: SparkStreamingApp[_]): DStream[Status] = {
    stream.filter(Sparkles.foldFilters(filter, moreFilters: _*)(booleanInstance.conjunction))
  }
}

object TwitterFilters {
 
  def country(code: String): Status => Boolean = s => {
    (for {
      place <- Option.apply(s.getPlace)
      countryCode <- Option.apply(place.getCountryCode)
    } yield {
      code == countryCode
    }).getOrElse(false)
  }
  
  def radius(latitude: Double, longitude: Double, radius: Double): Status => Boolean = s => {
    (for {
      loc <- Option.apply(s.getGeoLocation)
      lat <- Option.apply(loc.getLatitude)
      long <- Option.apply(loc.getLongitude)
    } yield {
      pow(lat - latitude, 2) + pow((long - longitude), 2) <= pow(radius, 2)
    }).getOrElse(false)     
  }
  
  def language(langCode: String): Status => Boolean = s => {
    (for {
      user <- Option.apply(s.getUser)
      lang <- Option.apply(user.getLang)
    } yield {
      lang == langCode
    }).getOrElse(false)    
  }
}