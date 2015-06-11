package wookie.yql.finance

import wookie.app.HttpToKafkaDumper
import wookie.yql.finance.StockCodecs._

object StocksCollector extends HttpToKafkaDumper[List[StockQuote]]
