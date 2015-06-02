package wookie.spark.cli

import org.rogach.scallop.ScallopConf

trait Input extends ScallopConf {
  lazy val inputURL = opt[String]("input", descr = "input URL", required = true)
}

trait Output extends ScallopConf {
  lazy val outputURL = opt[String]("output", descr = "output URL", required = true)
}

trait Name extends ScallopConf {
  lazy val name = opt[String]("name", descr = "name for application", required = true)
}

trait Duration extends ScallopConf {
  lazy val duration = opt[Long]("duration", descr = "Duration of mini batch in miliseconds", required = true)  
}

trait Twitter extends ScallopConf {
  lazy val consumerKey = opt[String]("consumer_key", descr = "Twitter OAuth consumer key", required = true)
  lazy val consumerSecret = opt[String]("consumer_secret", descr = "Twitter OAuth consumer secret", required = true)
  lazy val accessToken = opt[String]("access_token", descr = "Twitter OAuth access token", required = true)
  lazy val accessTokenSecret = opt[String]("access_token_secret", descr = "Twitter OAuth access token secret", required = true)
}

trait Filters extends ScallopConf {
  lazy val filters = opt[List[String]]("filters", descr = "Comma separated list of keywords to look for", default=Some(List[String]()))
}

trait Kafka extends ScallopConf {
  lazy val brokers = opt[List[String]]("brokers", descr = "Kafka Brokers host1:port1,host2:port2", required = true)
}

trait Topics extends ScallopConf {
  lazy val topics = opt[List[String]]("topics", descr = "Comma separated list of topics to subscribe to", default=Some(List[String]()))
}
