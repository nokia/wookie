package wookie.spark.streaming

import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.util.LengthUnit
import com.javadocmd.simplelatlng.window.CircularWindow
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.twitter.TwitterUtils
import twitter4j.Status
import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder
import wookie.spark.SparkStreamingApp
import wookie.spark.geo.Location
import wookie.spark.sparkle.Sparkle


case class Credentials(consumerKey: String, consumerSecret: String, accessToken: String, accessTokenSecret: String)

case class TwitterStream(credentials: Credentials, filters: Option[List[String]] = None) extends Sparkle[DStream[Status], SparkStreamingApp[_]] {
  def apply(app: SparkStreamingApp[_]): DStream[Status] = {
    val authorization = new OAuthAuthorization(new ConfigurationBuilder().
      setOAuthConsumerKey(credentials.consumerKey).
      setOAuthConsumerSecret(credentials.consumerSecret).
      setOAuthAccessToken(credentials.accessToken).
      setOAuthAccessTokenSecret(credentials.accessTokenSecret)
      build ())
    TwitterUtils.createStream(app.ssc, Some(authorization), filters.getOrElse(Nil))
  }

}

object TwitterMaps {

  val whitelist = "abcdefghijklmnopqrstuvwxyz 1234567890".toSet

  def location: Status => Option[Location] = s => {
    for {
      place <- Option.apply(s.getPlace)
      fullName <- Option.apply(place.getFullName)
      loc <- Option.apply {
        fullName.split(",", -1).toList match {
          case area :: region :: Nil => Location(area, region)
          case _ => null
        }
      }
    } yield {
      loc
    }
  }

  def latLong: Status => Option[LatLng] = s => {
    if (s.getGeoLocation == null) {
      None
    } else {
      Some(new LatLng(s.getGeoLocation.getLatitude, s.getGeoLocation.getLongitude))
    }
  }

  def urls: Status => List[String] = s => {
    val urlsInTweet = s.getURLEntities.map(_.getURL.toLowerCase)
    val mediaEntities = s.getMediaEntities.map(_.getURL.toLowerCase)
    (urlsInTweet ++ mediaEntities).toList
  }

  def refUsers: Status => List[String] = s => {
    s.getUserMentionEntities.map(_.getScreenName.toLowerCase).toList
  }

  def tags: Status => List[String] = s => {
    s.getHashtagEntities.map(_.getText.toLowerCase).toList
  }

  def user: Status => String = s => {
    s.getUser.getScreenName.toLowerCase
  }

  def text: Status => String = s => {
    val refsToRemove = urls(s) ++ refUsers(s) ++ tags(s)
    var result = s.getText.toLowerCase()
    refsToRemove.foreach(toRemove => result = result.replaceAllLiterally(toRemove, ""))
    result.filter(a => whitelist.contains(a)).trim
  }
}

object TwitterFilters {
  def country(code: String): Status => Boolean = s => {
    (for {
      place <- Option.apply(s.getPlace)
      countryCode <- Option.apply(place.getCountryCode)
    } yield {
      countryCode.equalsIgnoreCase(code)
    }).getOrElse(false)
  }
  
  def radius(center: LatLng, radiusInMeters: Double): Status => Boolean = s => {
    (for {
      loc <- Option.apply(s.getGeoLocation)
      lat <- Option.apply(loc.getLatitude)
      long <- Option.apply(loc.getLongitude)
    } yield {
      new CircularWindow(center, radiusInMeters, LengthUnit.METER).
      contains(new LatLng(lat, long))
    }).getOrElse(false)
  }
  
  def language(langCodePrefix: String): Status => Boolean = s => {
    (for {
      user <- Option.apply(s.getUser)
      lang <- Option.apply(user.getLang)
      langToCheck <- Option.apply(langCodePrefix)
    } yield {
      lang.toLowerCase.startsWith(langToCheck.toLowerCase)
    }).getOrElse(false)    
  }

}