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