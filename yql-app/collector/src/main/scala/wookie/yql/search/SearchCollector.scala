package wookie.yql.search

import wookie.collector.cli.HttpToKafkaDumper
import wookie.yql.search.SearchResultCodecs._

object SearchCollector extends HttpToKafkaDumper[List[SearchResult]]
