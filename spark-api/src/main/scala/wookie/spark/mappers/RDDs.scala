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

import wookie.{Containers, Bools}
import wookie.spark.Sparkle

import scala.reflect.ClassTag

object RDDs extends Containers[Sparkle, RDD] {

  override def fullJoin[A: ClassTag, B: ClassTag, C: ClassTag](rdd1: RDD[(A, B)], rdd2: RDD[(A, C)]): Sparkle[RDD[(A, (Option[B], Option[C]))]] = Sparkle {
    _ => rdd1.fullOuterJoin(rdd2)
  }

  override def leftJoin[A: ClassTag, B: ClassTag, C: ClassTag](rdd1: RDD[(A, B)], rdd2: RDD[(A, C)]): Sparkle[RDD[(A, (B, Option[C]))]] = Sparkle {
    _ => rdd1.leftOuterJoin(rdd2)
  }

  override def join[A: ClassTag, B: ClassTag, C: ClassTag](rdd1: RDD[(A, B)], rdd2: RDD[(A, C)]): Sparkle[RDD[(A, (B, C))]] = Sparkle {
    _ => rdd1.join(rdd2)
  }

  override def map[A, B: ClassTag](@transient rdd: RDD[A], func: A => B): Sparkle[RDD[B]] = Sparkle {
    _ => rdd.map ( value =>
      func(value)
    )
  }

  override def flatMap[A, B: ClassTag](@transient rdd: RDD[A], func: A => Traversable[B]): Sparkle[RDD[B]] = Sparkle {
    _ => rdd.flatMap ( value =>
      func(value)
    )
  }

  override def filter[A](rdd: RDD[A], filter: A => Boolean, moreFilters: (A => Boolean) *): Sparkle[RDD[A]] = Sparkle { _ =>
    rdd.filter(Bools.and(filter, moreFilters: _*))
  }

  override def sortByKey[A: ClassTag, B: ClassTag](rdd: RDD[(A, B)], ascending: Boolean)
                                                  (implicit ord: Ordering[A]): Sparkle[RDD[(A, B)]] = Sparkle { _ =>
    rdd.sortByKey(ascending)
  }
}
