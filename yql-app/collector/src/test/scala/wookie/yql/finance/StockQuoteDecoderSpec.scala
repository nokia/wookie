package wookie.yql.finance

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope
import argonaut._
import Argonaut._
import scalaz._

@RunWith(classOf[JUnitRunner])
class StockQuoteDecoderSpec extends Specification {
  
    //select * from yahoo.finance.quote where symbol in ("YHOO","AAPL","GOOG","MSFT")
  import StockCodecs.encoder
  "Should parse list of stock quotes from NY" in new context {
    val quote = quotes.decode(StockCodecs.decoder)
    println(quote)
    quote.getOrElse(Nil).map(r => 
      StockQuote(r.price, r.volume, r.symbol, r.change, 0L, r.captialization)) must contain(StockQuote(
        44.75, 7768294, "YHOO", -0.20, 0L, "42.00B"))
  }
  
   trait context extends Scope {
    val quotes = scala.io.Source.fromInputStream(this.getClass().getResourceAsStream("/stocks.json")).mkString
   }
}