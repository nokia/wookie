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
    searchResult.getOrElse(Nil) must contain(SearchResult(
        "Ebisu Sushi", "1283 9th Ave", "San Francisco",
        "CA", Some("(415) 566-1770"), 37.76444, -122.46666,
        4.5, 38, 38,
        Some(" This is the best sushi in SF so far. Its the freshest tasting and has some of the most original sushi dishes Ive found yet. " +
          "The bacon wrapped scallops, seafood salad and pink cadillac are especially worth noting. Service and atmosphere are nice as well. " +
          "I cant wait to try the next thing, their menu is loaded with tasty options.")
        ))
  }

   trait context extends Scope {
    val quotes = scala.io.Source.fromInputStream(this.getClass().getResourceAsStream("/local_search.json")).mkString
   }
}
