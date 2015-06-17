package wookie.spark.streaming

import scala.reflect.ClassTag

import org.apache.spark.streaming.dstream._
import org.apache.spark.streaming._
import wookie.spark.sparkle.Sparkle
import wookie.spark.SparkStreamingApp

case class JoinStreamInWindow[A: ClassTag, B: ClassTag, C: ClassTag](streamA: DStream[(A, B)], streamB: DStream[(A, C)],
                                                                     duration: Duration, slide: Duration)(implicit ord: Ordering[A])
  extends Sparkle[DStream[(A, (Option[B], Option[C]))], SparkStreamingApp[_]] {

   def apply(app: SparkStreamingApp[_]) = {
     val windowedStreamA = streamA.window(duration, slide)
     val windowedStreamB = streamB.window(duration, slide)
     windowedStreamA.fullOuterJoin(windowedStreamB)
   }
}
