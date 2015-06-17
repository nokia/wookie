package wookie.yql.analytics

import java.util.concurrent.TimeUnit

import argonaut.Argonaut._
import argonaut._

import scala.concurrent.duration.Duration


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
    } yield Weather(Duration(timestamp, TimeUnit.MILLISECONDS).toHours, lat, long, city, region, country, temp, conditions, chill,
        direction, speed, humidity, pressure, rising, visibility)
  }
  def parse(str: String): List[Weather] = {
    str.decodeEither[List[Weather]].getOrElse(Nil)
  }

  val queueName = "weather"
}
