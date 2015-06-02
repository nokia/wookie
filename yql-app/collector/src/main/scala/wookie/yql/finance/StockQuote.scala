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