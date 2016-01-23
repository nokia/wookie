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

import argonaut.Argonaut._
import argonaut._

case class Weather(timestamp: Long, latitude: Double, longitude: Double, area: String, region: String, country: String, temperature: Double,
                   conditions: String, windChill: Double, windDirection: Double, windSpeed: Double, humidity: Double,
                   pressure: Double, rising: Double, visibility: Double)

object Weather {
  implicit val decoder = DecodeJson {
    curs => for {
      timestamp <- (curs --\ "date").as[Long]
      temp <- (curs --\ "weather" --\ "temperature").as[Double]
      conditions <- (curs --\ "weather" --\  "conditions").as[String]
      chill <- (curs --\ "weather" --\ "windChill").as[Double]
      direction <- (curs --\ "weather" --\ "windDirection").as[Double]
      speed <- (curs --\ "weather" --\ "windSpeed").as[Double]
      humidity <- (curs --\ "weather" --\ "humidity").as[Double]
      pressure <- (curs --\ "weather" --\ "pressure").as[Double]
      rising <- (curs --\ "weather" --\ "rising").as[Double]
      visibility <- (curs --\ "weather" --\ "visibility").as[Double]
      lat <- (curs --\ "location" --\ "latitude").as[Double]
      long <- (curs --\ "location" --\ "longitude").as[Double]
      city <- (curs --\ "location" --\ "city").as[String]
      country <- (curs --\ "location" --\ "country").as[String]
      region <- (curs --\ "location" --\ "region").as[String]
    } yield Weather(timestamp, lat, long, city, region, country, temp, conditions, chill,
        direction, speed, humidity, pressure, rising, visibility)
  }
  def parse(str: String): List[Weather] = {
    str.decodeEither[List[Weather]].getOrElse(Nil)
  }

  val queueName = "weather"
}
