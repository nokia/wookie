package wookie.yql.news

import scala.language.postfixOps
import argonaut._
import Argonaut._
import scalaz._
import wookie.yql.common.CodecsUtils
import java.text.SimpleDateFormat

case class NewsEntry(title: String, content: Option[String]=None, language: String="en", 
    publisher: String, publishedDate: Long)
    
    

object NewsCodecs {
  implicit val decoder: DecodeJson[List[NewsEntry]] = DecodeJson {
    c =>
      val curs = c --\ "query" --\ "results" --\ "results"
      val news = CodecsUtils.loopOverArray(curs.downArray, decodeNews, DecodeResult(\/-(List[NewsEntry]())))
      val stories = CodecsUtils.loopOverArray(curs.downArray, decodeRelatedStories, DecodeResult(\/-(List[List[NewsEntry]]())))
      
      for {
        s <- stories
        n <- news
      } yield {
        n ++ s.flatten
      }
  }
  
  def decodeNews: ACursor => DecodeResult[NewsEntry] = {
    curs =>
      for {
        content <- parseContent(curs)
        title <-  (curs --\ "titleNoFormatting").as[String]
        language <- (curs --\ "language").as[String]
        publishedDate <- (curs --\ "publishedDate").as[String]
        publisher <-  (curs --\ "publisher").as[String]
      } yield {
        NewsEntry(title, content, language, publisher, parse(publishedDate))
      }
  }
  
  def parseContent: ACursor => DecodeResult[Option[String]] = curs => {
    val x = (curs --\ "content")
    if (x.succeeded) {
      x.as[Option[String]]
    } else {
      DecodeResult(\/-(None))
    }
  }
  
  def decodeRelatedStories: ACursor => DecodeResult[List[NewsEntry]] = curs => {
    val x = (curs --\ "relatedStories" \\)
    if (x.succeeded) {
      CodecsUtils.loopOverArray(x, decodeNews, DecodeResult(\/-(List[NewsEntry]())))
    } else {
      DecodeResult(\/-(List[NewsEntry]()))
    }
  }
  
  def parse(txt: String): Long = {
    val parser = new SimpleDateFormat("EEE, dd MMMM yyyy HH:mm:ss zzzz")
    parser.parse(txt).getTime
  }
  
  implicit val encoder: EncodeJson[NewsEntry] = {
    casecodec5(NewsEntry.apply, NewsEntry.unapply)("title", "content", "language", "publisher", "publishedDate")
  }  
}
