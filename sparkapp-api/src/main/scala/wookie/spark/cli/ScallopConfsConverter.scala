package wookie.spark.cli

import wookie.spark.streaming.Credentials

object ScallopConfsConverter {

  implicit def asCredentials(conf: Twitter): Credentials = {
    Credentials(conf.consumerKey(), conf.consumerSecret(), conf.accessToken(), conf.accessTokenSecret())
  }
}