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
    quote.getOrElse(Nil).map(r =>
      StockQuote(r.price, r.volume, r.symbol, r.change, 0L, r.captialization)) must contain(StockQuote(
        44.75, 7768294, "YHOO", -0.20, 0L, "42.00B"))
  }

   trait context extends Scope {
     val t = this.getClass.getResourceAsStream("/stocks.json")

    val quotes = scala.io.Source.fromInputStream(t).mkString
   }
}
