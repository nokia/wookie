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