package wookie.yql.places

import argonaut._
import Argonaut._
import scalaz._
import wookie.yql.common.CodecsUtils

case class Place(woeid: Long = 0, name: String = "", placeType: String = "", state: Option[String] = None,
                 country: String = "", latitude: Double = 0, longitude: Double = 0, swLat: Double = 0, swLong: Double = 0,
                 neLat: Double = 0, neLong: Double = 0, timezone: String = "")

object PlaceCodecs {
  implicit val decoder: DecodeJson[List[Place]] = DecodeJson {
    c =>
      val curs = c --\ "query" --\ "results" --\ "place"
      val x = curs.downArray

      val decodedList = if (x.succeeded) {
        CodecsUtils.loopOverArray(x, decodePlace, DecodeResult(\/-(List[Place]())))
      } else {
        decodePlace(curs).map(a => List(a))
      }
      for {
        places <- decodedList
      } yield {
        places.filter(pl => pl.placeType == "Town" && pl.state != None && pl.country == "United States")
      }
  }

  def decodePlace: ACursor => DecodeResult[Place] = {
    curs =>
      for {
        woeid <- (curs --\ "woeid").as[String]
        placeType <- (curs --\ "placeTypeName" --\ "content").as[String]
        name <- (curs --\ "name").as[String]
        state <- (curs --\ "admin1" --\ "code").as[String]
        country <- (curs --\ "country" --\ "content").as[String]
        latitude <- (curs --\ "centroid" --\ "latitude").as[String]
        longitude <- (curs --\ "centroid" --\ "longitude").as[String]

        swLatitude <- (curs --\ "boundingBox" --\ "southWest" --\ "latitude").as[String]
        swLongitude <- (curs --\ "boundingBox" --\ "southWest" --\ "longitude").as[String]
        neLatitude <- (curs --\ "boundingBox" --\ "northEast" --\ "latitude").as[String]
        neLongitude <- (curs --\ "boundingBox" --\ "northEast" --\ "longitude").as[String]
        timezone <- (curs --\ "timezone" --\ "content").as[String]
      } yield Place(woeid.toLong, name, placeType, parseState(state), country, latitude.toDouble,
        longitude.toDouble, swLatitude.toDouble, swLongitude.toDouble,
        neLatitude.toDouble, neLongitude.toDouble, timezone)
  }

  def parseState(state: String): Option[String] = {
    val stateStr = state.split("-", -1)
    if (stateStr.length != 2) {
      None
    } else {
      Some(stateStr(1))
    }
  }

  implicit val encoder: EncodeJson[Place] = {
    jencode11L((r: Place) => (r.woeid, r.name, r.state, r.country,
        r.latitude, r.longitude, r.swLat, r.swLong, r.neLat, r.neLong, r.timezone
        ) )("id", "name", "state", "country", "lat", "long", "swLat", "swLong", "neLat", "neLong", "timezone")
  }
}