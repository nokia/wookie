package wookie.yql.places

import wookie.collector.cli.HttpToKafkaDumper
import wookie.yql.places.PlaceCodecs._

object PlaceCollector extends HttpToKafkaDumper[List[Place]]
