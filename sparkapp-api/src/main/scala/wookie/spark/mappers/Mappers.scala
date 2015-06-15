package wookie.spark.mappers

import org.apache.spark.streaming.dstream.DStream
import shapeless._
import wookie.spark.SparkStreamingApp
import wookie.spark.sparkle.Sparkle

import scala.collection.immutable.{:: => Cons}
import scala.reflect.ClassTag

object Traversal {

  trait Traverser[A, L <: HList] extends DepFn2[A, L] with Serializable {
    type Out <: HList
  }

  object Traverser {
    def apply[A, L <: HList](implicit tr: Traverser[A, L]): Aux[A, L, tr.Out] = tr

    type Aux[A, L <: HList, Out0 <: HList] = Traverser[A, L] {type Out = Out0}

    implicit def hnilTraverser[A]: Aux[A, HNil, HNil] =
      new Traverser[A, HNil] {
        type Out = HNil

        def apply(elem: A, l: HNil): Out = l
      }

    implicit def hlistTraverser[A, H, T <: HList]
    (implicit st: Traverser[A, T]): Aux[A, (A => H) :: T, H :: st.Out] =
      new Traverser[A, (A => H) :: T] {
        type Out = H :: st.Out

        def apply(elem: A, l: (A => H) :: T) = l.head(elem) :: st(elem, l.tail)
      }
  }

}

object traverse {
  import Traversal._
  def apply[A, L <: HList](elem: A, l: L)(implicit tr: Traverser[A, L]): tr.Out = tr(elem, l)

  def traverser[A, L <: HList](elem: A, l: L)(implicit tr: Traverser[A, L]) = tr
}

object generic {

  def to[T <: Product, L <: HList, M <: HList](l: L)
                                     (implicit fooGen: Generic.Aux[T, M], eq: L =:= M): T = fooGen.from(l)
}

case class dstream[A, L <: HList](stream: DStream[A], mappers: L) {

  def map[O <: Product : ClassTag](tr: Traversal.Traverser[A, L])(implicit gen: Generic.Aux[O, tr.Out]): Sparkle[DStream[O], SparkStreamingApp[_]] = Sparkle { app =>
    stream.map { value =>
      val funcApplied = traverse(value, mappers)(tr)
      generic.to(funcApplied)
    }
  }
}


