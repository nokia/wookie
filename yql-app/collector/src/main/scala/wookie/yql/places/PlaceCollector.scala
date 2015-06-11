package wookie.yql.places

import wookie.app.HttpToKafkaDumper
import wookie.yql.places.PlaceCodecs._

object PlaceCollector extends HttpToKafkaDumper[List[Place]]
