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
import org.apache.spark.streaming.dstream.DStream
import shapeless._
import twitter4j.Status
import wookie.spark.cli.SparkStreamingApp
import wookie.spark.filters.FilterStream
import wookie.spark.geo.Location
import wookie.spark.mappers.{Maps, MapStream}
import wookie.spark.mappers.Maps.From
import wookie.spark.sparkle.Sparkle
import wookie.spark.streaming.twitter.TwitterFilters._
import wookie.spark.streaming.twitter.TwitterMaps._

case class Tweet(user: String, refUsers: List[String], refUrls: List[String], tags: List[String],
                 location: Option[Location], latLong: Option[LatLng], text: String)

case class LocalizedCleanedTwitterStream[B](credentials: Credentials, countryCode: String, languageCode: String,
                                         filters: Option[List[String]] = None, withId: Tweet => B)  extends Sparkle[DStream[(B, Tweet)], SparkStreamingApp[_]] {

  def extractors: (Status => String) :: (Status => List[String]) :: (Status => List[String]) :: (Status => List[String]) :: (Status => Option[Location]) ::
    (Status => Option[LatLng]) :: (Status => String) :: HNil = user :: refUsers :: urls :: tags :: location :: latLong :: text :: HNil

  def notEmptyTweet: Tweet => Boolean = t => !t.text.isEmpty

  def apply(app: SparkStreamingApp[_]): DStream[(B, Tweet)] = {
    val pipeline = for {
      tweets <- TwitterStream(credentials, filters)
      onlyUSEnglish <- FilterStream(tweets, country(countryCode), language(languageCode))
      cleanTweets <- MapStream(onlyUSEnglish, From(extractors).to[Tweet])
      notEmptyCleanTweets <- FilterStream(cleanTweets, notEmptyTweet)
      withId <- MapStream(notEmptyCleanTweets, Maps.withId(withId))
    } yield {
        withId
    }
    pipeline(app)
  }

}
