package wookie.yql.analytics

import wookie.spark.SparkApp
import org.rogach.scallop.ScallopConf
import wookie.spark.SparkStreamingApp
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.dstream.DStream
import wookie.spark.sparkle.streaming.TwitterStream
import wookie.spark.cli._

trait WeatherConf extends Name with Duration with Kafka

object WeatherClustering extends SparkStreamingApp[WeatherConf](new ScallopConf(_) with WeatherConf) {

  def runStreaming(opt: WeatherConf): Unit = {
    
  }
  def run(opt: WeatherConf, stream: DStream[(String, String)]): Unit = {
    stream.print()
  }
}

trait MediaMergerConf extends Name with Duration with Twitter

object MediaMerger extends SparkStreamingApp[MediaMergerConf](new ScallopConf(_) with MediaMergerConf) {
  
  import ScallopConfsConverter._
  
  def runStreaming(opt: MediaMergerConf): Unit = {
    
    val pipeline = for {
      tweets <- new TwitterStream(opt) 
    } yield {
      tweets.print()
    }
    pipeline.run(this)
    ()
  }
}