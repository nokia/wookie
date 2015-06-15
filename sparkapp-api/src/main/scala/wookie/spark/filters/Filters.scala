package wookie.spark.filters

import org.apache.spark.streaming.dstream.DStream
import spire.algebra.Monoid
import wookie.spark.SparkStreamingApp
import wookie.spark.sparkle.Sparkle

object Conjunction extends Monoid[Boolean] with Serializable {
  def op(f1: Boolean, f2: Boolean) = f1 && f2

  def id: Boolean = true
}

object Disjunction extends Monoid[Boolean] with Serializable {
  def op(f1: Boolean, f2: Boolean) = f1 || f2

  def id: Boolean = false
}

case class FilterBy[A](stream: DStream[A], filter: A => Boolean, moreFilters: (A => Boolean) *) extends Sparkle[DStream[A], SparkStreamingApp[_]] {

  def apply(app: SparkStreamingApp[_]): DStream[A] = {
    stream.filter(FilterBy.and(filter, moreFilters: _*))
  }
}

object FilterBy {

  def fold[A](f1: A => Boolean, fs: (A => Boolean) *)(boolM: Monoid[Boolean]): A => Boolean = s => {
    fs.foldLeft(f1(s))((t1, t2) => boolM.op(t2(s), t1))
  }

  def or[A](f1: A => Boolean, fs: (A => Boolean) *): (A => Boolean) = fold(f1, (fs): _ *)(Disjunction)

  def and[A](f1: A => Boolean, fs: (A => Boolean) *): A => Boolean = fold(f1, (fs): _ *)(Conjunction)

}
