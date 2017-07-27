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
package wookie

import scala.reflect.ClassTag

/**
  * API for running operations through runtime environment monad
  * @tparam CONTAINER
  */
trait Container[CONTAINER[_]] {
  def map[A, B: ClassTag](@transient container: CONTAINER[A], func: A => B): Sparkle[CONTAINER[B]]

  def flatMap[A, B: ClassTag](@transient container: CONTAINER[A], func: A => Traversable[B]): Sparkle[CONTAINER[B]]

  def fullJoin[A: ClassTag, B: ClassTag, C: ClassTag](container1: CONTAINER[(A, B)], container2: CONTAINER[(A, C)]):
  Sparkle[CONTAINER[(A, (Option[B], Option[C]))]]

  def leftJoin[A: ClassTag, B: ClassTag, C: ClassTag](container1: CONTAINER[(A, B)], container2: CONTAINER[(A, C)]): Sparkle[CONTAINER[(A, (B, Option[C]))]]

  def join[A: ClassTag, B: ClassTag, C: ClassTag](container1: CONTAINER[(A, B)], container2: CONTAINER[(A, C)]): Sparkle[CONTAINER[(A, (B, C))]]

  def filter[A](container: CONTAINER[A], filter: A => Boolean, moreFilters: (A => Boolean) *): Sparkle[CONTAINER[A]]

  def sortByKey[A: ClassTag, B: ClassTag](container: CONTAINER[(A, B)], ascending: Boolean = true)
                                         (implicit ord: Ordering[A]): Sparkle[CONTAINER[(A, B)]]

}
