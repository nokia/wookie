package wookie.spark.geo

import com.javadocmd.simplelatlng.LatLng

object GeoData {
  val countries: Map[String, (LatLng, Double)] = Map(
    ("US",  (new LatLng(39.828175, -98.5795), 2253.0 * 1000.0))
  )

  val defaultCountry: (LatLng, Double) = (new LatLng(39.828175, -98.5795), 2253.0 * 1000.0)

  def countryCenter(countryCode: String) = countries.getOrElse(countryCode, defaultCountry)._1
  def countryRadius(countryCode: String) = countries.getOrElse(countryCode, defaultCountry)._2
}

case class Location(area: String, region: String)
