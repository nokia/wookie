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
package wookie.yql.news

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope
import argonaut._
import Argonaut._
import scalaz._

@RunWith(classOf[JUnitRunner])
class NewsEntryDecoderSpec extends Specification {

  //select * from google.news where q = "new york"
  import NewsCodecs.encoder
  "Should parse list of news from NY" in new context {
    val news = newYorkNews.decode(NewsCodecs.decoder)
    news.getOrElse(Nil) must contain(NewsEntry("NY grants first banking license to bitcoin exchange",
        publisher="CNBC", publishedDate=1431010800000L))
  }

    "Should parse list of news from LA" in new context {
      val news = laNews.decode(NewsCodecs.decoder)
      news.getOrElse(Nil) must contain(NewsEntry("Los Angeles Clippers Need Chris Paul to Win Series",
          publisher="Hoops Habit", publishedDate=1431014850000L))
    }

  trait context extends Scope {
    val newYorkNews = scala.io.Source.fromInputStream(this.getClass().getResourceAsStream("/newyork_news.json")).mkString
    val laNews = scala.io.Source.fromInputStream(this.getClass().getResourceAsStream("/los_angeles.json")).mkString
  }
}
