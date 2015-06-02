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
    println(loc.getOrElse(Nil).mkString("\n"))
    loc.getOrElse(Nil) must contain(sunnyvale)
  }
  
  "multi Location should be parseable" in new context {
    val loc = multiLocResponse.decode(PlaceCodecs.decoder)
    val l = loc.getOrElse(Nil)
    println(l.mkString("\n"))
    println(l.asJson.nospaces)
    loc.getOrElse(Nil) must containAllOf(List(sunnyvale, newyork))
  }  
  
  
  trait context extends Scope {
    val singleLocResponse = scala.io.Source.fromInputStream(this.getClass().getResourceAsStream("/sunnyvale_place.json")).mkString
    val multiLocResponse = scala.io.Source.fromInputStream(this.getClass().getResourceAsStream("/multiple_places.json")).mkString
    val sunnyvale = Place(2502265,"Sunnyvale","Town",Some("CA"),"United States",37.371609,-122.038254,37.332298,-122.065659,37.44899,-121.982407,"America/Los_Angeles")
    val newyork = Place(2459115,"New York","Town",Some("NY"),"United States",40.71455,-74.007118,40.477421,-74.258904,40.917622,-73.700378,"America/New_York")
  }

}