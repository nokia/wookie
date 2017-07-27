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

import shapeless._

/**
  * Mapping objects helpers
  */
object Mappers {
  // scalastyle:off
  case class from[A, L <: HList, M <: HList](mappers: L)(implicit tr: Applicator.Aux[A, L, M]) {
    def to[B](value: A)(implicit gen: Generic.Aux[B, M]): B = {
      gen.from(tr(value, mappers))
    }
  }
  //scalastyle:on

  def withFunction[A, B](f: A => B)(elem: A): (B, A) = {
    (f(elem), elem)
  }

  def withCount[A, B](f: A => B)(elem: A): (B, Int) = {
    (f(elem), 1)
  }
}

/**
  * Function application
  * @tparam A type of left operand function
  * @tparam L list of functions mappings
  */
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
