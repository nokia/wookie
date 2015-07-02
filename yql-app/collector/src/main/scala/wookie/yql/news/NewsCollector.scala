package wookie.yql.news

import NewsCodecs._
import wookie.collector.cli.HttpToKafkaDumper

object NewsCollector extends HttpToKafkaDumper[List[NewsEntry]]