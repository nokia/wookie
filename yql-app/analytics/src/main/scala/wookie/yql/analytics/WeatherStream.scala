package wookie.yql.analytics

import org.apache.spark.streaming.dstream.DStream
import wookie.spark.sparkle.Sparkle
import wookie.spark.SparkStreamingApp
import wookie.spark.sparkle.streaming.KafkaConsumerStringStream
import wookie.spark.sparkle.Sparkles
import argonaut._
import Argonaut._
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit


case class Weather(timestamp: Long, latitude: Double, longitude: Double, locationName: String, temperature: Double, conditions: String, windChill: Double,
                            windDirection: Double, windSpeed: Double, humidity: Double, pressure: Double, rising: Double, visibility: Double)



case class WeatherStream(brokers: List[String]) extends Sparkle[DStream[Weather], SparkStreamingApp[_]] {
  
  implicit val decoder: DecodeJson[Weather] = DecodeJson {
    curs => for {
        timestamp <- (curs --\ "date").as[Long]
        temp <- (curs --\ "item" --\ "condition" --\ "temp").as[Double]
        conditions <- (curs --\ "item" --\ "condition" --\ "text").as[String]
        chill <- (curs --\ "wind" --\ "chill").as[Double]
        direction <- (curs --\ "wind" --\ "direction").as[Double]
        speed <- (curs --\ "wind" --\ "speed").as[Double]
        humidity <- (curs --\ "atmosphere" --\ "humidity").as[Double]
        pressure <- (curs --\ "atmosphere" --\ "pressure").as[Double]
        rising <- (curs --\ "atmosphere" --\ "rising").as[Double]
        visibility <- (curs --\ "atmosphere" --\ "visibility").as[Double]
        lat <- (curs --\ "item" --\ "lat").as[Double]
        long <- (curs --\ "item" --\ "long").as[Double]
        city <- (curs --\ "location" --\ "city").as[String]
        country <- (curs --\ "location" --\ "country").as[String]
        region <- (curs --\ "location" --\ "region").as[String]
      } yield Weather(Duration(timestamp, TimeUnit.MILLISECONDS).toHours, lat, long, s"$city, $region, $country", temp, conditions, chill,
          direction, speed, humidity, pressure, rising, visibility)
  }
  
  def run(app: SparkStreamingApp[_]): DStream[Weather] = {
    
    val pipeline = for {
      queueInput <- KafkaConsumerStringStream(brokers, Set("weather"))
      weatherStream <- Sparkles.flatMap(queueInput) { i=> 
        val readings = i._2
        readings.decodeOr((x:List[Weather]) => x, List[Weather]())
      }
    } yield {
      weatherStream
    }
    pipeline.run(app)
  }
  
}