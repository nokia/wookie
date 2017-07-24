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
package wookie.yql.places

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import argonaut._
import Argonaut._
import scalaz._

@RunWith(classOf[JUnitRunner])
class PlaceDecoderSpec extends Specification {

  //select * from geo.places where text="san francisco, ca" or text="San Jose, CA"

  import PlaceCodecs._

  "Single Location should be parseable" in new context {
    val loc = singleLocResponse.decode(PlaceCodecs.decoder)
    loc.getOrElse(Nil) must contain(sunnyvale)
  }

  "multi Location should be parseable" in new context {
    val loc = multiLocResponse.decode(PlaceCodecs.decoder)
    val l = loc.getOrElse(Nil)
    loc.getOrElse(Nil) must containAllOf(List(sunnyvale, newyork))
  }

  trait context extends Scope {
    val singleLocResponse = scala.io.Source.fromInputStream(this.getClass().getResourceAsStream("/sunnyvale_place.json")).mkString
    val multiLocResponse = scala.io.Source.fromInputStream(this.getClass().getResourceAsStream("/multiple_places.json")).mkString
    val sunnyvale = Place(2502265,"Sunnyvale","Town",Some("CA"),"United States",37.371609,-122.038254,37.332298,-122.065659,37.44899,-121.982407,
      "America/Los_Angeles")
    val newyork = Place(2459115,"New York","Town",Some("NY"),"United States",40.71455,-74.007118,40.477421,-74.258904,40.917622,-73.700378,"America/New_York")
  }

}
