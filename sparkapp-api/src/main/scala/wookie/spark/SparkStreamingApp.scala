package wookie.spark

import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.Milliseconds
import org.apache.spark.streaming.StreamingContext

import scalaz.std.option._
import scalaz.syntax.applicative._
import wookie.spark.cli._

abstract class SparkStreamingApp[A <: Name with Duration](options: Array[String] => A) extends SparkApp[A](options) {

  protected var _ssc: StreamingContext = _
  def ssc = _ssc

  def runStreaming(opt: A): Unit
  
  final def run(opt: A): Unit = {
    _ssc = new StreamingContext(sc, Milliseconds(opt.duration()))
    setStreamingLogLevels()
    
    runStreaming(opt)
    
     _ssc.start()
    _ssc.awaitTermination()
  }
  
  def setStreamingLogLevels() = {
    val log4jInitialized = Logger.getRootLogger.getAllAppenders.hasMoreElements
    if (!log4jInitialized) {
      Logger.getRootLogger.setLevel(Level.WARN)
    }
  }
}