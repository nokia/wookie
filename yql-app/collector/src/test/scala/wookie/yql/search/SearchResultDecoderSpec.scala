package wookie.yql.search

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope

import argonaut._
import Argonaut._
import scalaz._

@RunWith(classOf[JUnitRunner])
class SearchResultDecoderSpec extends Specification {
  //select * from local.search where query="sushi" and location="san francisco, ca"
import SearchResultCodecs.encoder
  "Should parse list of search results" in new context {
    val searchResult = quotes.decode(SearchResultCodecs.decoder)
    println(searchResult)
    searchResult.getOrElse(Nil) must contain(SearchResult(
        "Ebisu Sushi", "1283 9th Ave", "San Francisco", 
        "CA", Some("(415) 566-1770"), 37.76444, -122.46666,
        4.5, 38, 38, 
        Some(" This is the best sushi in SF so far. Its the freshest tasting and has some of the most original sushi dishes Ive found yet. The bacon wrapped scallops, seafood salad and pink cadillac are especially worth noting. Service and atmosphere are nice as well. I cant wait to try the next thing, their menu is loaded with tasty options.")
        ))
  }
  
   trait context extends Scope {
    val quotes = scala.io.Source.fromInputStream(this.getClass().getResourceAsStream("/local_search.json")).mkString
   }  
}