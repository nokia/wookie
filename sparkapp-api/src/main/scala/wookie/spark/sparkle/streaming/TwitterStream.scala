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
import com.javadocmd.simplelatlng.window.CircularWindow
import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.util.LengthUnit


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
  
  def radius(latitude: Double, longitude: Double, radiusInMeters: Double): Status => Boolean = s => {
    (for {
      loc <- Option.apply(s.getGeoLocation)
      lat <- Option.apply(loc.getLatitude)
      long <- Option.apply(loc.getLongitude)
    } yield {
      new CircularWindow(new LatLng(latitude, longitude), radiusInMeters, LengthUnit.METER).
      contains(new LatLng(lat, long))
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