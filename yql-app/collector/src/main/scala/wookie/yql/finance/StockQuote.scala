/* Copyright (C) 2014-2015 by Nokia.
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
*/
package wookie.yql.finance

import argonaut._
import Argonaut._
import scalaz._
import wookie.yql.common.CodecsUtils

case class StockQuote(price: Double, volume: Long, symbol: String, change: Double, date: Long,
    captialization: String)

object StockCodecs {
  implicit val decoder: DecodeJson[List[StockQuote]] = DecodeJson {
    c =>
      val currentTime = System.currentTimeMillis()
      val curs = c --\ "query" --\ "results" --\ "quote"
      CodecsUtils.loopOverArray(curs.downArray, decodeStocks(currentTime), DecodeResult(\/-(List[StockQuote]())))
  }

  def decodeStocks(dateTime: Long): ACursor => DecodeResult[StockQuote] = {
    curs =>
      for {
        price <- (curs --\ "LastTradePriceOnly").as[String]
        volume <-  (curs --\ "Volume").as[String]
        symbol <- (curs --\ "Symbol").as[String]
        change <- (curs --\ "Change").as[String]
        capitalization <-  (curs --\ "MarketCapitalization").as[String]
      } yield {
        StockQuote(price.toDouble, volume.toLong, symbol, change.toDouble,
           dateTime, capitalization)
      }
  }

  implicit val encoder: EncodeJson[StockQuote] = {
    casecodec6(StockQuote.apply, StockQuote.unapply)("price", "volume", "symbol",
        "change", "date", "captialization")
  }
}