package wookie.spark.sparkle.streaming

import scala.reflect.ClassTag
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.rogach.scallop.ScallopConf
import kafka.serializer.Decoder
import kafka.serializer.StringDecoder
import wookie.spark.SparkStreamingApp
import wookie.spark.sparkle.Sparkle

abstract class KafkaConsumerStream[K: ClassTag, V: ClassTag, KD <: Decoder[K]: ClassTag, VD <: Decoder[V]: ClassTag]
  (brokers: List[String], topics: Set[String]) extends Sparkle[DStream[(K, V)], SparkStreamingApp[_]] {
  
  def apply(app: SparkStreamingApp[_]): DStream[(K, V)] = {
    val brokersList = brokers.map(_.trim).mkString(",")
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokersList)
    KafkaUtils.createDirectStream[K, V, KD, VD](app.ssc, kafkaParams, topics)
  }

}

case class KafkaConsumerStringStream(brokers: List[String], topics: Set[String]) extends 
  KafkaConsumerStream[String, String, StringDecoder, StringDecoder](brokers, topics)

case class KafkaTypedStream[A: ClassTag](brokers: List[String], topic: String, parser: String => List[A]) extends Sparkle[DStream[A], SparkStreamingApp[_]] {

  def apply(app: SparkStreamingApp[_]): DStream[A] = {
    val pipeline = for {
      queueInput <- KafkaConsumerStringStream(brokers, Set(topic))
      weatherStream <- Sparkle { (app: SparkStreamingApp[_]) =>
        queueInput.flatMap( i => parser(i._2) )
      }
    } yield {
        weatherStream
      }
    pipeline(app)
  }

}