package wookie.spark.streaming.kafka.cli

import org.rogach.scallop.ScallopConf

trait Kafka extends ScallopConf {
  lazy val brokers = opt[List[String]]("brokers", descr = "Kafka Brokers host1:port1,host2:port2", required = true)
}
