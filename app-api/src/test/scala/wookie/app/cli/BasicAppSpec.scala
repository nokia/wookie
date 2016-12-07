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

import org.junit.runner.RunWith
import org.scalacheck._
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BasicAppSpec extends Specification with ScalaCheck {

  def urls: Arbitrary[String] = {
    Arbitrary(for {
      prefix <- Gen.oneOf("http://", "https://", "s3://")
      suffix <- Gen.containerOf[List, String](Gen.oneOf(Gen.const("."), Gen.const("/"), Gen.identifier))
    } yield prefix + suffix.mkString(""))
  }

  def alphaNumericMap: Arbitrary[Map[String, String]] = {
    val tupleGen = for {
      a <- Gen.identifier
      value <- Gen.oneOf(Gen.const('='), Gen.alphaNumChar)
      b <- Gen.containerOf[Array, Char](value)
    } yield (a, new String(b))
    Arbitrary(Gen.mapOf(tupleGen))
  }

  def alphaNumericList: Arbitrary[List[String]] = {
    Arbitrary(Gen.containerOf[List, String](Gen.identifier))
  }

  "Basic App needs to make properties accessible in run method" >> prop { (inputURL: String, query: Map[String, String],
                                                                           topics: List[String]) =>
    val queryStr = query.map { case (a, b) => s"$a=$b" }.mkString("&")
    val topicList = topics.map(a => List("--topics", a)).foldLeft(List[String]())((a, b) => a ++ b)
    val cli = List("--input", inputURL, "--query", queryStr) ++ topicList
    MockBasicApp.main(cli.toArray)
    MockBasicApp.inputURL must_== inputURL
    MockBasicApp.query must_== query
    MockBasicApp.topics must_== topics
  }.setArbitraries(urls, alphaNumericMap, alphaNumericList)
}


