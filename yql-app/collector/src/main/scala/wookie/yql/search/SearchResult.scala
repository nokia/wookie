package wookie.yql.search

import argonaut._
import Argonaut._
import scalaz._
import wookie.yql.common.CodecsUtils

case class SearchResult(title: String, address: String, city: String, 
    state: String, phone: Option[String], latitude: Double = 0, longitude: Double = 0, 
    avgRating: Double = 0, totalRatings: Long = 0, totalReviews: Long = 0, lastReview: Option[String] = None)

object SearchResultCodecs {
  implicit val decoder: DecodeJson[List[SearchResult]] = DecodeJson {
    c =>
      val curs = c --\ "query" --\ "results" --\ "Result"
      CodecsUtils.loopOverArray(curs.downArray, decodeSearchResult, DecodeResult(\/-(List[SearchResult]())))
  }
  
  def decodeSearchResult: ACursor => DecodeResult[SearchResult] = {
    curs =>
      for {
        title <- (curs --\ "Title").as[String]
        address <-  (curs --\ "Address").as[String]
        city <- (curs --\ "City").as[String]
        state <- (curs --\ "State").as[String]
        phone <-  (curs --\ "Phone").as[Option[String]]
        latitude <-  (curs --\ "Latitude").as[String]
        longitude <-  (curs --\ "Longitude").as[String]
        avgRating <-  (curs --\ "Rating" --\ "AverageRating").as[String]
        totalRatings <-  (curs --\ "Rating" --\ "TotalRatings").as[String]
        totalReviews <-  (curs --\ "Rating" --\ "TotalReviews").as[String]
        lastReview <-  (curs --\ "Rating" --\ "LastReviewIntro").as[Option[String]]
      } yield {
        SearchResult(title, address, city, state, phone, latitude.toDouble, longitude.toDouble, 
            avgRating.toDouble, totalRatings.toLong, totalReviews.toLong, lastReview)
      }
  }  

  implicit val encoder: EncodeJson[SearchResult] = {
    casecodec11(SearchResult.apply, SearchResult.unapply)("title", "address", "city",
      "state", "phone", "latitude", "longitude", "avgRating", "totalRatings", "totalReviews", 
      "lastReview")
  }

}