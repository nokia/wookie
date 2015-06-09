package wookie.yql.analytics

import org.apache.spark.streaming.Minutes
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.dstream.InputDStream
import org.rogach.scallop.ScallopConf
import wookie.spark.SparkApp
import wookie.spark.SparkStreamingApp
import wookie.spark.cli._
import wookie.spark.sparkle.Sparkles._
import wookie.spark.sparkle.streaming.JoinStreamInWindow
import wookie.spark.sparkle.streaming.KafkaConsumerStringStream
import wookie.spark.sparkle.streaming.TwitterStream
import wookie.spark.sparkle.streaming.TwitterFilters._
import wookie.spark.sparkle.streaming.FilterTweetsBy

trait WeatherConf extends Name with Duration with Kafka

object WeatherClustering extends SparkStreamingApp[WeatherConf](new ScallopConf(_) with WeatherConf) {

  def runStreaming(opt: WeatherConf): Unit = {
    
  }
  def run(opt: WeatherConf, stream: DStream[(String, String)]): Unit = {
    stream.print()
  }
}

trait MediaMergerConf extends Name with Duration with Twitter with Kafka

object MediaMerger extends SparkStreamingApp[MediaMergerConf](new ScallopConf(_) with MediaMergerConf) {
  
  import ScallopConfsConverter._
  
  def runStreaming(opt: MediaMergerConf): Unit = {
    
    val pipeline = for {
      tweets <- TwitterStream(opt)
      onlyUSEnglish <- FilterTweetsBy(tweets, country("US"), language("en"))
      weather <- WeatherStream(opt.brokers())
//      joined <- new JoinStreamInWindow(onlyUS, weather, Minutes(60))
    } yield {
      onlyUSEnglish.print()
    }
    pipeline.run(this)
    ()
  }
}