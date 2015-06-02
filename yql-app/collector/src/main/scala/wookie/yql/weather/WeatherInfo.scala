package wookie.yql.weather

import argonaut._
import Argonaut._
import scalaz._
import java.text.SimpleDateFormat
import wookie.yql.common.CodecsUtils

case class WeatherCondition(temperature: Double, conditions: String, windChill: Double,
                            windDirection: Double, windSpeed: Double, humidity: Double, pressure: Double, rising: Double, visibility: Double)

case class Location(latitude: Double, longitude: Double, city: String, country: String, region: String)

case class Weather(date: Long, location: Location, weather: WeatherCondition)

object WeatherDecoders {
  implicit val decoder: DecodeJson[List[Weather]] = DecodeJson {
    c =>
      val curs = c --\ "query" --\ "results" --\ "channel"
      val x = curs.downArray

      if (x.succeeded) {
        CodecsUtils.loopOverArray(x, decodeCondition, DecodeResult(\/-(List())))
      } else {
        decodeCondition(curs).map(a => List(a))
      }

  }

  def decodeCondition: ACursor => DecodeResult[Weather] = {
    curs =>
      for {
        date <- (curs --\ "item" --\ "condition" --\ "date").as[String]
        temp <- (curs --\ "item" --\ "condition" --\ "temp").as[String]
        txt <- (curs --\ "item" --\ "condition" --\ "text").as[String]
        chill <- (curs --\ "wind" --\ "chill").as[String]
        direction <- (curs --\ "wind" --\ "direction").as[String]
        speed <- (curs --\ "wind" --\ "speed").as[String]
        humidity <- (curs --\ "atmosphere" --\ "humidity").as[String]
        pressure <- (curs --\ "atmosphere" --\ "pressure").as[String]
        rising <- (curs --\ "atmosphere" --\ "rising").as[String]
        visibility <- (curs --\ "atmosphere" --\ "visibility").as[String]
        lat <- (curs --\ "item" --\ "lat").as[String]
        long <- (curs --\ "item" --\ "long").as[String]
        city <- (curs --\ "location" --\ "city").as[String]
        country <- (curs --\ "location" --\ "country").as[String]
        region <- (curs --\ "location" --\ "region").as[String]
      } yield Weather(parse(date), Location(lat.toDouble, long.toDouble, city, country, region), WeatherCondition(temp.toDouble, txt, chill.toDouble, direction.toDouble, speed.toDouble,
        humidity.toDouble, pressure.toDouble, rising.toDouble, visibility.toDouble))
  }
  
  def parse(s: String): Long = {
    val parser = new SimpleDateFormat("EEE, dd MMMM yyyy hh:mm aa zzz")
    parser.parse(s).getTime
  }
        
  implicit val locationEncoder: EncodeJson[Location] = {
    casecodec5(Location.apply, Location.unapply)("latitude", "longitude", "city", "country", "region")
  }   
  
  implicit val conditionEncoder: EncodeJson[WeatherCondition] = {
    casecodec9(WeatherCondition.apply, WeatherCondition.unapply)("temperature", "conditions", "windChill", "windDirection", "windSpeed", "humidity", "pressure", "rising", "visibility")
  }   
  
  implicit val readingEncoder: EncodeJson[Weather] = {
    jencode3L((r: Weather) => (r.date, r.location, r.weather) )("date", "location", "weather")
  }
  
}
