package wookie.yql.search

import wookie.app.HttpToKafkaDumper
import wookie.yql.search.SearchResultCodecs._

object SearchCollector extends HttpToKafkaDumper[List[SearchResult]]
