package wookie.spark.streaming

import scala.reflect.ClassTag
import org.apache.hadoop.mapreduce.InputFormat
import org.apache.spark.streaming.dstream.InputDStream
import wookie.spark.SparkStreamingApp
import wookie.spark.sparkle.Sparkle
import org.apache.spark.streaming.dstream.DStream

sealed trait HdfsStream[K] extends Sparkle[K, SparkStreamingApp[_]]

case class HdfsCustomStream[K: ClassTag, V: ClassTag, F <: InputFormat[K, V]: ClassTag](url: String) extends HdfsStream[DStream[(K, V)]] {
  def apply(app: SparkStreamingApp[_]): DStream[(K, V)] = {
    app.ssc.fileStream[K, V, F](url)
  }
}

case class HdfsTextStream(url: String) extends HdfsStream[DStream[String]] {
  def apply(app: SparkStreamingApp[_]): DStream[String] = {
    app.ssc.textFileStream(url)
  }
}
