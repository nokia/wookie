package wookie.spark.mappers

import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream
import shapeless._
import wookie.spark.{SparkApp, SparkStreamingApp}
import wookie.spark.sparkle.Sparkle

import scala.collection.immutable.{:: => Cons}
import scala.reflect.ClassTag

trait Applicator[A, L <: HList] extends DepFn2[A, L] with Serializable {
  type Out <: HList
}

object Applicator {
  def apply[A, L <: HList](implicit tr: Applicator[A, L]): Aux[A, L, tr.Out] = tr

  type Aux[A, L <: HList, Out0 <: HList] = Applicator[A, L] {type Out = Out0}

  implicit def hnilApplicator[A]: Aux[A, HNil, HNil] =
    new Applicator[A, HNil] {
      type Out = HNil

      def apply(elem: A, l: HNil): Out = l
    }

  implicit def hlistApplicator[A, H, T <: HList]
  (implicit st: Applicator[A, T]): Aux[A, (A => H) :: T, H :: st.Out] =
    new Applicator[A, (A => H) :: T] {
      type Out = H :: st.Out

      def apply(elem: A, l: (A => H) :: T) = l.head(elem) :: st(elem, l.tail)
    }
}

case class MapStream[A, B: ClassTag](@transient stream: DStream[A], func: A => B) extends Sparkle[DStream[B], SparkStreamingApp[_]] {
  def apply(app: SparkStreamingApp[_]): DStream[B] = {
    stream.map ( value =>
      func(value)
    )
  }
}

case class MapRDD[A, B: ClassTag](@transient rdd: RDD[A], func: A => B) extends Sparkle[RDD[B], SparkApp[_]] {
  def apply(app: SparkApp[_]): RDD[B] = {
    rdd.map ( value =>
      func(value)
    )
  }
}

object Maps {

  case class from[A, L <: HList, M <: HList](mappers: L)(implicit tr: Applicator.Aux[A, L, M]) {
    def to[B](value: A)(implicit gen: Generic.Aux[B, M]): B = {
      gen.from(tr(value, mappers))
    }
  }

}