package wookie.spark.mappers

import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.Time
import org.apache.spark.streaming.dstream.DStream
import shapeless._
import wookie.spark.cli.{SparkApp, SparkStreamingApp}
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
    stream.map { value =>
      func(value)
    }
  }
}

case class MapRDD[A, B: ClassTag](@transient rdd: RDD[A], func: A => B) extends Sparkle[RDD[B], SparkApp[_]] {
  def apply(app: SparkApp[_]): RDD[B] = {
    rdd.map ( value =>
      func(value)
    )
  }
}

case class FlatMapStream[A, B: ClassTag](@transient stream: DStream[A], func: A => Traversable[B]) extends Sparkle[DStream[B], SparkStreamingApp[_]] {
  def apply(app: SparkStreamingApp[_]): DStream[B] = {
    stream.flatMap { value =>
      func(value)
    }
  }
}

case class FlatMapRDD[A, B: ClassTag](@transient rdd: RDD[A], func: A => Traversable[B]) extends Sparkle[RDD[B], SparkApp[_]] {
  def apply(app: SparkApp[_]): RDD[B] = {
    rdd.flatMap ( value =>
      func(value)
    )
  }
}

case class TransformStreamWithTime[A: ClassTag, B: ClassTag](stream: DStream[A], transformFunc: (RDD[A], Time) => RDD[B]) extends Sparkle[DStream[B], SparkStreamingApp[_]] {
  def apply(app: SparkStreamingApp[_]): DStream[B] = {
    stream.transform(transformFunc)
  }
}

case class TransformStream[A: ClassTag, B: ClassTag](stream: DStream[A], transformFunc: RDD[A] => RDD[B]) extends Sparkle[DStream[B], SparkStreamingApp[_]] {
  def apply(app: SparkStreamingApp[_]): DStream[B] = {
    stream.transform(transformFunc)
  }
}

case class SortStreamByKey[A: ClassTag, B: ClassTag](stream: DStream[(A, B)], ascending: Boolean = true)(implicit ord: Ordering[A]) extends Sparkle[DStream[(A, B)], SparkStreamingApp[_]] {
  def apply(app: SparkStreamingApp[_]): DStream[(A, B)] = {
    stream.transform(_.sortByKey(ascending))
  }
}

case class JoinStreams[A: ClassTag, B: ClassTag, C: ClassTag](stream1: DStream[(A, B)], stream2: DStream[(A, C)])
  extends Sparkle[DStream[(A, (B, C))], SparkStreamingApp[_]] {

  def apply(app: SparkStreamingApp[_]): DStream[(A, (B, C))] = {
    stream1.join(stream2)
  }
}

case class LeftJoinStreams[A: ClassTag, B: ClassTag, C: ClassTag](stream1: DStream[(A, B)], stream2: DStream[(A, C)])
  extends Sparkle[DStream[(A, (B, Option[C]))], SparkStreamingApp[_]] {

  def apply(app: SparkStreamingApp[_]): DStream[(A, (B, Option[C]))] = {
    stream1.leftOuterJoin(stream2)
  }
}

case class FullJoinStreams[A: ClassTag, B: ClassTag, C: ClassTag](stream1: DStream[(A, B)], stream2: DStream[(A, C)])
  extends Sparkle[DStream[(A, (Option[B], Option[C]))], SparkStreamingApp[_]] {

  def apply(app: SparkStreamingApp[_]): DStream[(A, (Option[B], Option[C]))] = {
    stream1.fullOuterJoin(stream2)
  }
}

case class JoinRDD[A: ClassTag, B: ClassTag, C: ClassTag](rdd1: RDD[(A, B)], rdd2: RDD[(A, C)])
  extends Sparkle[RDD[(A, (B, C))], SparkApp[_]] {

  def apply(app: SparkApp[_]): RDD[(A, (B, C))] = {
    rdd1.join(rdd2)
  }
}

case class LeftJoinRDD[A: ClassTag, B: ClassTag, C: ClassTag](rdd1: RDD[(A, B)], rdd2: RDD[(A, C)])
  extends Sparkle[RDD[(A, (B, Option[C]))], SparkApp[_]] {

  def apply(app: SparkApp[_]): RDD[(A, (B, Option[C]))] = {
    rdd1.leftOuterJoin(rdd2)
  }
}

case class FullJoinRDD[A: ClassTag, B: ClassTag, C: ClassTag](rdd1: RDD[(A, B)], rdd2: RDD[(A, C)])
  extends Sparkle[RDD[(A, (Option[B], Option[C]))], SparkApp[_]] {

  def apply(app: SparkApp[_]): RDD[(A, (Option[B], Option[C]))] = {
    rdd1.fullOuterJoin(rdd2)
  }
}

object Maps {

  case class from[A, L <: HList, M <: HList](mappers: L)(implicit tr: Applicator.Aux[A, L, M]) {
    def to[B](value: A)(implicit gen: Generic.Aux[B, M]): B = {
      gen.from(tr(value, mappers))
    }
  }

  def withId[A, B](f: A => B)(elem: A): (B, A) = {
    (f(elem), elem)
  }

  def withCount[A, B](f: A => B)(elem: A): (B, Int) = {
    (f(elem), 1)
  }
}