package wookie.yql.search

import wookie.app.HttpToKafkaDumper

import SearchResultCodecs._
object SearchCollector extends HttpToKafkaDumper
