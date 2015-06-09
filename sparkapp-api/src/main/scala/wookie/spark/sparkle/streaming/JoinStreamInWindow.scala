package wookie.spark.sparkle.streaming

import scala.reflect.ClassTag

import org.apache.spark.streaming.dstream._
import org.apache.spark.streaming._
import wookie.spark.sparkle.Sparkle
import wookie.spark.SparkStreamingApp

case class JoinStreamInWindow[A, B, C](streamA: DStream[(A, B)], streamB: DStream[(A, C)], duration: Duration)(implicit a: ClassTag[A], 
    b: ClassTag[B], c: ClassTag[C], ord: Ordering[A]) extends Sparkle[DStream[(A, (Option[B], Option[C]))], SparkStreamingApp[_]] {
   def run(app: SparkStreamingApp[_]): DStream[(A, (Option[B], Option[C]))] = {
     
     val windowedStreamA = streamA.window(duration)
     val windowedStreamB = streamB.window(duration)
     windowedStreamA.fullOuterJoin(windowedStreamB)
   }
}