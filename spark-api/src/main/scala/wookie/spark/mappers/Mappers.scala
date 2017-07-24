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
package wookie.spark.mappers

import org.apache.spark.rdd.RDD
import shapeless._
import wookie.spark.sparkle.Sparkle

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

object Mappers {

  case class From[A, L <: HList, M <: HList](mappers: L)(implicit tr: Applicator.Aux[A, L, M]) {
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

  def fullJoin[A: ClassTag, B: ClassTag, C: ClassTag](rdd1: RDD[(A, B)], rdd2: RDD[(A, C)]): Sparkle[RDD[(A, (Option[B], Option[C]))]] = Sparkle {
    _ => rdd1.fullOuterJoin(rdd2)
  }

  def leftJoin[A: ClassTag, B: ClassTag, C: ClassTag](rdd1: RDD[(A, B)], rdd2: RDD[(A, C)]): Sparkle[RDD[(A, (B, Option[C]))]] = Sparkle {
    _ => rdd1.leftOuterJoin(rdd2)
  }

  def join[A: ClassTag, B: ClassTag, C: ClassTag](rdd1: RDD[(A, B)], rdd2: RDD[(A, C)]): Sparkle[RDD[(A, (B, C))]] = Sparkle {
    _ => rdd1.join(rdd2)
  }

  def map[A, B: ClassTag](@transient rdd: RDD[A], func: A => B): Sparkle[RDD[B]] = Sparkle {
    _ => rdd.map ( value =>
      func(value)
    )
  }

  def flatMap[A, B: ClassTag](@transient rdd: RDD[A], func: A => Traversable[B]): Sparkle[RDD[B]] = Sparkle {
    _ => rdd.flatMap ( value =>
      func(value)
    )
  }

}
