package wookie.app.cli

import org.rogach.scallop.ScallopConf

trait Kafka extends ScallopConf {
  lazy val brokers = opt[List[String]]("brokers", descr = "Kafka Brokers host1:port1,host2:port2", required = true)  
}

trait Topics extends ScallopConf {
  lazy val topics = opt[List[String]]("topics", descr = "Comma separated list of topics to publish to", default=Some(List[String]()), required=true)
}

trait Input extends ScallopConf {
  lazy val inputURL = opt[String]("input", descr = "Input URL", required = true)
}

trait URLQuery extends ScallopConf {
  lazy val query = opt[Map[String, String]]("query", descr = "Parameters of the http request in a form of key1=value1&key2=value2",
    default = Some(Map[String, String]()))(URLQueryConverter)
}

trait Port extends ScallopConf {
  lazy val port = opt[Int]("port", descr = "Port to listent to", required = true)
}
