package wookie.collector.kafka

import scalaz.stream._
import scalaz.stream.io._
import scalaz.concurrent._
import scala.collection.JavaConverters._
import java.util.concurrent.{ Future => JFuture }
import scalaz._
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata

object KafkaProducerStream {

  def sink(brokers: List[String], topics: List[String]): Sink[Task, String] = {
    resource(Task.delay(createKafkaClient(brokers)))(os => Task.delay(os.close))(
      os => Task.now((bytes: String) => push(topics, bytes.hashCode.toString, bytes)(os)   ))
  }
  
  def push(topics: List[String], id: String, msg: String): KafkaProducer[Nothing, Nothing] => Task[Unit] = client => {
    val tasks = for {
      topic <- topics
    } yield {
      val record = new ProducerRecord(topic, id, msg)
      val future = client.send(record.asInstanceOf[ProducerRecord[Nothing, Nothing]])
      jFutureToTask(future.asInstanceOf[JFuture[Unit]])
    }
    Task.gatherUnordered(tasks, true).map( r => ())
  }  
  
  private def createKafkaClient(brokers: List[String]) = {
    new KafkaProducer(Map[String, Object](
    "bootstrap.servers" -> brokers.mkString(","),
    "value.serializer" -> "org.apache.kafka.common.serialization.StringSerializer",
    "acks" -> "1",
    "timeout.ms" -> "5000",
    "metadata.fetch.timeout.ms" -> "5000",
    "retries" -> "0",
    "key.serializer" -> "org.apache.kafka.common.serialization.StringSerializer").asJava)
  }
  
  private def jFutureToTask[T](fut: JFuture[T]): Task[T] = {
    val fu = Future {
      \/.fromTryCatchNonFatal(fut.get)
    }
    new Task(fu)
  }  
    
}