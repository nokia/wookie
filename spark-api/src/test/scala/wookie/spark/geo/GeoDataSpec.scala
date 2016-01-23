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

import java.util.Locale

import org.junit.runner.RunWith
import org.scalacheck.{Gen, Arbitrary}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

/**
  * Created by ljastrze on 11/29/15.
  */
@RunWith(classOf[JUnitRunner])
class GeoDataSpec extends Specification with ScalaCheck {

  val countryCodes = Locale.getISOCountries().toSeq

  val countryCodeGen = Gen.oneOf(Gen.const[String](null), Gen.oneOf(countryCodes), Gen.alphaStr)

  "Should always return country center and radius" >> prop { (countryCode: String) =>
    val center = GeoData.countryCenter(countryCode)
    val radius = GeoData.countryRadius(countryCode)
    Option(center) must beSome
    Option(radius) must beSome
  }.setArbitrary(Arbitrary(countryCodeGen))

  "Should return default country center and radius if not found" >> prop { (countryCode: String) =>
    val center = GeoData.countryCenter(countryCode)
    val radius = GeoData.countryRadius(countryCode)
    center.getLatitude must beCloseTo(39.8d, 0.1d)
    center.getLongitude must beCloseTo(-98.5d, 0.1d)
    radius must beCloseTo(2253.0 * 1000.0, 10d)
  }.setArbitrary(Arbitrary(Gen.alphaStr))

  "Should order locations alphabetically" >> prop { (area1: String, region1: String, area2: String, region2: String) =>
    val l1 = Location(area1, region1)
    val l2 = Location(area2, region2)
    val ordering = Location.locOrder
    ordering.compare(l1, l2) must_== (l1.toString compareTo l2.toString)
  }


}
