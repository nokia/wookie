/* Copyright (C) 2014-2015 by Nokia.
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
*/
package wookie.app.cli

import org.rogach.scallop.ScallopConf

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

