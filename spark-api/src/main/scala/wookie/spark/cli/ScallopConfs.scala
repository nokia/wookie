/*
 * Copyright (C) 2014-2015 by Nokia.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
  lazy val checkpointDir = opt[String]("checkpointDir", descr="Checkpointing directory", required=false)
}
