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
package wookie.collector.streams

import argonaut._
import Argonaut._
import org.http4s._
import org.http4s.argonaut.jsonOf
import org.http4s.client._

import scalaz.\/
import scalaz.concurrent.Task
import scalaz.stream.Process

import wookie.collector.streams.StreamUtils.once
/**
  * Created by ljastrze on 11/16/15.
  */
object HttpStream {

  def source(request: Request): Client => Process[Task, Response] = client => {
    once(Task.delay(client))(cli => cli.shutdown()) { cli =>
      cli(request)
    }
  }

  def createRequest(url: String, queryParams: Map[String, String]): \/[ParseFailure, Request] = {
    def withQueries(uri: Uri): Uri = {
      uri.setQueryParams(queryParams.mapValues(Seq(_))).asInstanceOf[Uri]
    }
    for {
      parsedUrl <- Uri.fromString(url)
    } yield Request(uri=withQueries(parsedUrl))
  }
}

object JsonTransformer {
  def asObject[A](resp: Response)(implicit dec: DecodeJson[A]): Task[A] = {
    implicit val decoder = jsonOf[A]
    resp.as[A]
  }

  def asText[A](resp: Response)(implicit dec: DecodeJson[A], enc: EncodeJson[A]): Task[String] = {
    implicit val decoder = jsonOf[A]
    resp.as[A].map { in =>
      in.asJson.nospaces
    }
  }
}
