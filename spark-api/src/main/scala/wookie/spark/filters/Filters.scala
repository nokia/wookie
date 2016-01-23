/*
 * Copyright (C) 2014-2015 by Nokia.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package wookie.spark.filters

import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream
import spire.algebra.Monoid
import wookie.spark.cli.{SparkApp, SparkStreamingApp}
import wookie.spark.sparkle.Sparkle

object Conjunction extends Monoid[Boolean] with Serializable {
  def op(f1: Boolean, f2: Boolean): Boolean = f1 && f2

  def id: Boolean = true
}

object Disjunction extends Monoid[Boolean] with Serializable {
  def op(f1: Boolean, f2: Boolean): Boolean = f1 || f2

  def id: Boolean = false
}

case class FilterStream[A](stream: DStream[A], filter: A => Boolean, moreFilters: (A => Boolean) *) extends Sparkle[DStream[A], SparkStreamingApp[_]] {

  def apply(app: SparkStreamingApp[_]): DStream[A] = {
    stream.filter(Filters.and(filter, moreFilters: _*))
  }
}

case class FilterRDD[A](rdd: RDD[A], filter: A => Boolean, moreFilters: (A => Boolean) *) extends Sparkle[RDD[A], SparkApp[_]] {

  def apply(app: SparkApp[_]): RDD[A] = {
    rdd.filter(Filters.and(filter, moreFilters: _*))
  }
}

object Filters {

  def fold[A](f1: A => Boolean, fs: (A => Boolean) *)(boolM: Monoid[Boolean]): A => Boolean = s => {
    fs.foldLeft(f1(s))((t1, t2) => boolM.op(t2(s), t1))
  }

  def or[A](f1: A => Boolean, fs: (A => Boolean) *): (A => Boolean) = fold(f1, fs: _ *)(Disjunction)

  def and[A](f1: A => Boolean, fs: (A => Boolean) *): A => Boolean = fold(f1, fs: _ *)(Conjunction)

}
