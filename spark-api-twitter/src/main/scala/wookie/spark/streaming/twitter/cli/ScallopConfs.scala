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
package wookie.spark.streaming.twitter.cli

import org.rogach.scallop.ScallopConf
import wookie.spark.streaming.twitter.Credentials

object TwitterConverter {

  implicit def asCredentials(conf: Twitter): Credentials = {
    Credentials(conf.consumerKey(), conf.consumerSecret(), conf.accessToken(), conf.accessTokenSecret())
  }
}

trait Twitter extends ScallopConf {
  lazy val consumerKey = opt[String]("consumer_key", descr = "Twitter OAuth consumer key", required = true)
  lazy val consumerSecret = opt[String]("consumer_secret", descr = "Twitter OAuth consumer secret", required = true)
  lazy val accessToken = opt[String]("access_token", descr = "Twitter OAuth access token", required = true)
  lazy val accessTokenSecret = opt[String]("access_token_secret", descr = "Twitter OAuth access token secret", required = true)
}
