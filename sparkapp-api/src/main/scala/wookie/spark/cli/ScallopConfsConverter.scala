package wookie.spark.cli

import wookie.spark.sparkle.streaming.Credentials

object ScallopConfsConverter {

  implicit def asCredentials(conf: Twitter): Credentials = {
    Credentials(conf.consumerKey(), conf.consumerSecret(), conf.accessToken(), conf.accessTokenSecret())
  }
}