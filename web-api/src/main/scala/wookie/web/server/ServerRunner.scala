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
package wookie.web.server

import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.{Server, HttpService}
import wookie.web.cli.Port
import scalaz.effect._

object ServerRunner {

  type ServiceConfiguration = (Map[String, HttpService], Port)

  def start: ServiceConfiguration => IO[Server] = env =>
    IO {
      def mountServices(builder: BlazeBuilder, services: List[(String, HttpService)]): BlazeBuilder = services match {
        case hd :: tail => mountServices(builder.mountService(hd._2, hd._1), tail)
        case _          => builder
      }
      val (services, conf) = env
      mountServices(BlazeBuilder, services.toList).bindHttp(conf.port(), "0.0.0.0").
        withNio2(true).run
    }

  def stop(server: Server): IO[Server] = IO {
    server.shutdownNow()
  }

  def restart(server: Server): ServiceConfiguration => IO[Server] = env =>
    for {
      _ <- stop(server)
      s <- start(env)
    } yield s

  def awaits(server: Server): IO[Server] = IO {
    server.awaitShutdown()
    server
  }

}
