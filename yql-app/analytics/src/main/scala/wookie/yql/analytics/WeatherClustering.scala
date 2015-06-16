package wookie.yql.analytics

import org.apache.spark.streaming.dstream.DStream
import org.rogach.scallop.ScallopConf
import wookie.spark.SparkStreamingApp
import wookie.spark.cli.{Kafka, Duration, Name}

trait WeatherConf extends Name with Duration with Kafka

object WeatherClustering extends SparkStreamingApp[WeatherConf](new ScallopConf(_) with WeatherConf) {

  def runStreaming(opt: WeatherConf): Unit = {

  }
  def run(opt: WeatherConf, stream: DStream[(String, String)]): Unit = {
    stream.print()
  }
}