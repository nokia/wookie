package wookie.app

import wookie.app.cli._
import org.rogach.scallop.ScallopConf
import scalaz.stream._
import scalaz.concurrent._
import com.ning.http.client.Response
import wookie.http.HttpStream
import wookie.messages.KafkaProducerStream
import argonaut.EncodeJson
import argonaut.DecodeJson

trait HttpToKafkaDumperConf extends HttpConsumerConf with Kafka with Topics

class HttpToKafkaDumper[A](implicit decoder: DecodeJson[A], encoder: EncodeJson[A]) extends HttpConsumer[HttpToKafkaDumperConf](new ScallopConf(_) with HttpToKafkaDumperConf) {
  
  def createPipeline(opt: HttpToKafkaDumperConf, input: Process[Task, Response]): Process[Task, Unit] = {
    input.map(_.getResponseBody).pipe(HttpStream.transformJson).
      to(KafkaProducerStream.sink(opt.brokers(), opt.topics()))
  }
}
