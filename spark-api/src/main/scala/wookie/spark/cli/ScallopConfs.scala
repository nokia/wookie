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

trait Filters extends ScallopConf {
  lazy val filters = opt[List[String]]("filters", descr = "Comma separated list of keywords to look for", default=Some(List[String]()))
}

trait Topics extends ScallopConf {
  lazy val topics = opt[List[String]]("topics", descr = "Comma separated list of topics to subscribe to", default=Some(List[String]()))
}

trait Checkpoint extends ScallopConf {
  lazy val checkpointDir = opt[String]("checkpointDir", descr="Checkpointing directory", required=true)
  lazy val useCheckpoint = opt[Boolean]("useCheckpoint", descr="Use checkpointing directory", required=true, default = Some(false))
}