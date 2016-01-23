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
package wookie.yql.common

import argonaut._
import Argonaut._

object CodecsUtils {

    def loopOverArray[A](curs: ACursor, func: ACursor => DecodeResult[A], accum: DecodeResult[List[A]]): DecodeResult[List[A]] = {
    if (curs.succeeded) {

      val currentDecodedValues = func(curs)
      val updatedAccum = accum.map {
        a =>
          currentDecodedValues.value match {
            case Some(b) => b :: a
            case None    => a
          }
      }
      loopOverArray(curs.right, func, updatedAccum)
    } else {
      accum
    }
  }
}
