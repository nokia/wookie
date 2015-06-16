package wookie.spark.mappers

import org.apache.spark.streaming.dstream.DStream
import shapeless._
import wookie.spark.SparkStreamingApp
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

case class dstream[A, L <: HList, M <: HList](mappers: L)(implicit tr: Applicator.Aux[A, L, M]) {

//  def map[O](implicit gen: Generic.Aux[O, M], tag: ClassTag[O]): Sparkle[DStream[O], SparkStreamingApp[_]] = Sparkle { app =>
//    val result = stream.map { value =>
//      gen.from(tr(value, mappers))
//    }
//    println(result)
//    result
//  }

  def map2[O](stream: DStream[A])(implicit gen: Generic.Aux[O, M], tag: ClassTag[O]): DStream[O] = {
    val result = stream.map { value =>
      gen.from(tr(value, mappers))
    }
    println(result)
    result
  }
}


