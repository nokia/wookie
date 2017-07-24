/*
 * Copyright (C) 2014-2015 by Nokia.
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
 *
 */
package wookie.yql.analytics

import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.util.LengthUnit
import com.javadocmd.simplelatlng.window.CircularWindow
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.twitter.TwitterUtils
import org.rogach.scallop.ScallopConf
import shapeless.HNil
import twitter4j.Status
import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder
import wookie.spark.mappers.Mappers.From
import wookie.spark.mappers.{Mappers, StreamMappers}
import wookie.yql.geo.Location
import wookie.spark.sparkle.StreamingSparkle

object TwitterConverter {

  implicit def asCredentials(conf: TwitterConf): Credentials = {
    Credentials(conf.consumerKey(), conf.consumerSecret(), conf.accessToken(), conf.accessTokenSecret())
  }
}

trait TwitterConf extends ScallopConf {
  lazy val consumerKey = opt[String]("consumer_key", descr = "Twitter OAuth consumer key", required = true)
  lazy val consumerSecret = opt[String]("consumer_secret", descr = "Twitter OAuth consumer secret", required = true)
  lazy val accessToken = opt[String]("access_token", descr = "Twitter OAuth access token", required = true)
  lazy val accessTokenSecret = opt[String]("access_token_secret", descr = "Twitter OAuth access token secret", required = true)
}

case class Credentials(consumerKey: String, consumerSecret: String, accessToken: String, accessTokenSecret: String)

case class Tweet(user: String, refUsers: List[String], refUrls: List[String], tags: List[String],
                 location: Option[Location], latLong: Option[LatLng], text: String)

object Twitter {

  import wookie.spark.filters.Filters._
  import StreamMappers._

  val whitelist: Set[Char] = "abcdefghijklmnopqrstuvwxyz 1234567890".toSet

  def location: Status => Option[Location] = s => {
    for {
      place <- Option.apply(s.getPlace)
      fullName <- Option.apply(place.getFullName)
      loc <-
        fullName.split(",", -1).toList match {
          case area :: region :: Nil => Some(Location(area.trim, region.trim))
          case _ => None
        }
    } yield {
      loc
    }
  }

  def latLong: Status => Option[LatLng] = s => {
    Option(s.getGeoLocation).map { loc =>
      new LatLng(loc.getLatitude, loc.getLongitude)
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

  private def extractors = user :: refUsers :: urls :: tags :: location :: latLong :: text :: HNil
  def notEmptyTweet: Tweet => Boolean = t => !t.text.isEmpty
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

  def twitterStream(credentials: Credentials, filters: Option[List[String]] = None):
  StreamingSparkle[DStream[Status]] = StreamingSparkle {
    ssc =>
      val authorization = new OAuthAuthorization(new ConfigurationBuilder().
        setOAuthConsumerKey(credentials.consumerKey).
        setOAuthConsumerSecret(credentials.consumerSecret).
        setOAuthAccessToken(credentials.accessToken).
        setOAuthAccessTokenSecret(credentials.accessTokenSecret)
        build ())
      TwitterUtils.createStream(ssc, Some(authorization), filters.getOrElse(Nil))
  }

  def cleanedTwitterStreamWithLocations[B](credentials: Credentials, countryCode: String, languageCode: String,
                                           filters: Option[List[String]] = None, withId: Tweet => B): StreamingSparkle[DStream[(B, Tweet)]] = StreamingSparkle {
    ssc =>
      val pipeline = for {
        tweets <- twitterStream(credentials, filters)
        onlyUSEnglish <- filterStream(tweets, country(countryCode), language(languageCode))
        cleanTweets <- map(onlyUSEnglish, From(extractors).to[Tweet])
        notEmptyCleanTweets <- filterStream(cleanTweets, notEmptyTweet)
        withId <- map(notEmptyCleanTweets, Mappers.withId(withId))
      } yield {
        withId
      }
      pipeline.run(ssc)
  }
}
