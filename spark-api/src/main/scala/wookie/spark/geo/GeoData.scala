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
package wookie.spark.geo

import com.javadocmd.simplelatlng.LatLng

import scala.math.Ordering

object GeoData {
  val countries: Map[String, (LatLng, Double)] = Map(
    ("US",  (new LatLng(39.828175, -98.5795), 2253.0 * 1000.0))
  )

  val defaultCountry: (LatLng, Double) = (new LatLng(39.828175, -98.5795), 2253.0 * 1000.0)

  def countryCenter(countryCode: String): LatLng = countries.getOrElse(countryCode, defaultCountry)._1
  def countryRadius(countryCode: String): Double = countries.getOrElse(countryCode, defaultCountry)._2
}

case class Location(area: String, region: String)

object Location {
  implicit val locOrder: Ordering[Location] = new Ordering[Location] {
    def compare(x: Location, y: Location): Int = x.toString compareTo y.toString
  }
}
