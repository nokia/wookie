package wookie.yql.finance

import wookie.collector.cli.HttpToKafkaDumper
import wookie.yql.finance.StockCodecs._

object StocksCollector extends HttpToKafkaDumper[List[StockQuote]]
