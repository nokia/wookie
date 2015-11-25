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
package wookie.collector.cli

import argonaut._
import org.http4s._
import org.log4s._
import org.rogach.scallop.ScallopConf
import wookie.app.cli.{BasicApp, Input, Topics, URLQuery}
import wookie.collector.streams.{JsonDumper, HttpStream, Config}

import scalaz._

trait AppConf extends Input with URLQuery with Kafka with Topics

class KafkaPusherApp[A](config: List[String] => Config)
                       (implicit decoder: DecodeJson[A], encoder: EncodeJson[A]) extends BasicApp[AppConf](new ScallopConf(_) with AppConf) {

  private[this] val log = getLogger

  override def run(opt: AppConf): Unit = {
    val environment = config(opt.brokers())
    val runResult = runPipeline(opt.inputURL(), opt.query(), opt.topics())(environment)
    runResult.leftMap( exp => throw exp)
    log.info(s"Exit result: $runResult")
  }

  def parseRequest(baseUrl: String, queryParams: Map[String, String]): ParseFailure \/ Request = {
    HttpStream.createRequest(baseUrl, queryParams)
  }

  def toKeyValue(str: String): (String, String) = (str.hashCode.toString, str)

  def runPipeline(baseUrl: String, queryParams: Map[String, String],
                     topics: List[String]): Config => \/[Throwable, Unit] = config => {
    for {
      req <- parseRequest(baseUrl, queryParams).leftMap(failure => new RuntimeException(failure.details).asInstanceOf[Throwable])
      exec <- JsonDumper(decoder, encoder).push(req, topics, toKeyValue)(config).run.attemptRun
    } yield {
      exec
    }
  }
}
