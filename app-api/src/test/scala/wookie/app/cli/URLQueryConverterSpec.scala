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
package wookie.app.cli

import _root_.org.specs2.mutable._
import _root_.org.junit.runner.RunWith
import _root_.org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope

@RunWith(classOf[JUnitRunner])
class URLQueryConverterSpec extends Specification {

  "Query should parse be parseable" in new context {
    val txt = "format=json&q=select * from weather.forecast where woeid in (select woeid from geo.places(1) " +
      "where text=\"Sunnyvale, CA\" or text=\"San Jose, CA\")"
    val map = parse(txt)
    map must equalTo (Map("format" -> "json", "q" ->
      "select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"Sunnyvale, CA\" or text=\"San Jose, CA\")"))
  }

  "Single value" in new context {
    val txt = "format=json=x"
    val map = parse(txt)
    map must equalTo (Map("format" -> "json=x"))
  }

  "Empty value" in new context {
    val txt = "format="
    val map = parse(txt)
    map must equalTo (Map("format" -> ""))
  }

  "No equals sign value" in new context {
    val txt = "format"
    val map = parse(txt)
    map must equalTo (Map("format" -> ""))
  }

  "ghost keys" in new context {
    val txt = "&&q&&"
    val map = parse(txt)
    map must equalTo (Map("q" -> ""))
  }

  "empty keys" in new context {
    val txt = ""
    val map = parse(txt)
    map must equalTo (Map())
  }

  "empty keys2" in new context {
    val txt = "   "
    val map = parse(txt)
    map must equalTo (Map())
  }

  "empty keys3" in new context {
    val txt = "    &   &   &  & "
    val map = parse(txt)
    map must equalTo (Map())
  }

  trait context extends Scope {
    def parse(s: String): Map[String, String] = {
      URLQueryConverter.parse(List(("x", List(s)))).right.get.get
    }
  }
}
