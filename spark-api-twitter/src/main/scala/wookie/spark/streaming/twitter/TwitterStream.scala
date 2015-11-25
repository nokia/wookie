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
package wookie.spark.streaming.twitter

import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.util.LengthUnit
import com.javadocmd.simplelatlng.window.CircularWindow
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.twitter.TwitterUtils
import twitter4j.Status
import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder
import wookie.spark.cli.SparkStreamingApp
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
      place <- Option(s.getPlace)
      fullName <- Option(place.getFullName)
      loc <- fullName.split(",", -1).toList match {
          case area :: region :: Nil => Some(Location(area.trim, region.trim))
          case _ => None
        }
    } yield {
      loc
    }
  }

  def latLong: Status => Option[LatLng] = s => {
    Option(s.getGeoLocation).map(loc => new LatLng(loc.getLatitude, loc.getLongitude))
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