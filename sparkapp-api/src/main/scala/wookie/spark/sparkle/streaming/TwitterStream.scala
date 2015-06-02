package wookie.spark.sparkle.streaming

import wookie.spark.sparkle.Sparkle
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import twitter4j.Status
import wookie.spark.SparkApp
import org.rogach.scallop.ScallopConf
import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder
import org.apache.spark.streaming.twitter.TwitterUtils
import wookie.spark.SparkStreamingApp

case class Credentials(consumerKey: String, consumerSecret: String, accessToken: String, accessTokenSecret: String)

case class TwitterStream(credentials: Credentials, filters: Option[List[String]] = None) extends Sparkle[ReceiverInputDStream[Status], SparkStreamingApp[_]] {
  def run(app: SparkStreamingApp[_]): ReceiverInputDStream[Status] = {
    val authorization = new OAuthAuthorization(new ConfigurationBuilder().
      setOAuthConsumerKey(credentials.consumerKey).
      setOAuthConsumerSecret(credentials.consumerSecret).
      setOAuthAccessToken(credentials.accessToken).
      setOAuthAccessTokenSecret(credentials.accessTokenSecret)
      build ())
    TwitterUtils.createStream(app.ssc, Some(authorization), filters.getOrElse(Nil))
  }
}
