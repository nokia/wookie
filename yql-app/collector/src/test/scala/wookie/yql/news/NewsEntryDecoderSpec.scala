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
    printJson(news)
    news.getOrElse(Nil) must contain(NewsEntry("NY grants first banking license to bitcoin exchange", 
        publisher="CNBC", publishedDate=1431010800000L))
  }

    "Should parse list of news from LA" in new context {
      val news = laNews.decode(NewsCodecs.decoder)
      //printJson(news)
      news.getOrElse(Nil) must contain(NewsEntry("Los Angeles Clippers Need Chris Paul to Win Series", 
          publisher="Hoops Habit", publishedDate=1431014850000L))
    }  

  trait context extends Scope {
    val newYorkNews = scala.io.Source.fromInputStream(this.getClass().getResourceAsStream("/newyork_news.json")).mkString
    val laNews = scala.io.Source.fromInputStream(this.getClass().getResourceAsStream("/los_angeles.json")).mkString
    def printJson(news: \/[\/[String, (String, CursorHistory)], List[NewsEntry]]) = {
      if (news.isLeft) {
        println(news)
      } else {
        val x = news.getOrElse(Nil)
        for {
          i <- x
        } {
          println(i.asJson.nospaces)
        }
      }
    }
  }
}