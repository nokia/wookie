package wookie.spark.cli

import org.apache.log4j.{Level, Logger}
import org.apache.spark.streaming.{Milliseconds, StreamingContext}

abstract class SparkStreamingApp[A <: Name with Duration with Checkpoint](options: Array[String] => A) extends SparkApp[A](options) {

  protected var _ssc: StreamingContext = _
  def ssc = _ssc

  private def createStreamingContext(opt: A): () => StreamingContext = {
    () =>
      _ssc = new StreamingContext(sc, Milliseconds(opt.duration()))
      setStreamingLogLevels()
      runStreaming(opt)
      opt.checkpointDir.map { chkPoint =>
        _ssc.checkpoint(chkPoint)
      }
      _ssc
  }

  def runStreaming(opt: A): Unit

  final def run(opt: A): Unit = {
    if (opt.checkpointDir.isDefined) {
      _ssc = StreamingContext.getOrCreate(opt.checkpointDir(), createStreamingContext(opt), createOnError = true)
    } else {
      _ssc = createStreamingContext(opt)()
    }
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