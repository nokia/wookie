package wookie.yql.analytics

import org.apache.spark.streaming.dstream.DStream
import org.rogach.scallop.ScallopConf
import wookie.spark.cli.{Checkpoint, SparkStreamingApp, Duration, Name}
import wookie.spark.streaming.kafka.cli.Kafka

trait WeatherConf extends Name with Duration with Kafka with Checkpoint

object WeatherClustering extends SparkStreamingApp[WeatherConf](new ScallopConf(_) with WeatherConf) {

  def runStreaming(opt: WeatherConf): Unit = {

  }
  def run(opt: WeatherConf, stream: DStream[(String, String)]): Unit = {
    stream.print()
  }
}