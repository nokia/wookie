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

package wookie.spark.sparkle

import org.junit.runner.RunWith
import org.specs2.ScalaCheck
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.typelevel.discipline.specs2.mutable.Discipline
import wookie.spark.cli.{SparkStreamingApp, SparkApp}

/**
  * Created by ljastrze on 11/29/15.
  */
@RunWith(classOf[JUnitRunner])
class SparkleSpec extends Specification with Mockito with ScalaCheck {

  val sparkApp = mock[SparkApp[_]]
  val streamingApp = mock[SparkStreamingApp[_]]

  "Identity Law in Sparkle" >> prop { (value: String) =>
    val sparkle = Sparkle(value)
    val mapped = sparkle.map(identity)
    mapped(sparkApp) must_== sparkle(sparkApp)
  }

  "Identity Law in StreamingSparkle" >> prop { (value: String) =>
    val sparkle = StreamingSparkle(value)
    val mapped = sparkle.map(identity)
    mapped(streamingApp) must_== sparkle(streamingApp)
  }

  "Left identity" >> prop { (value1: String, value2: String) =>
    val sparkle1 = Sparkle(value1)
    val sparkle2 = Sparkle(value2)
    sparkle1.flatMap(x => sparkle2)(sparkApp) must_== sparkle2(sparkApp)
  }

  "Right identity" >> prop { (value1: String) =>
    val sparkle1 = Sparkle(value1)
    sparkle1.flatMap(x => Sparkle(x))(sparkApp) must_== sparkle1(sparkApp)
  }

  "Associativity"  >> prop { (value1: String, value2: String, value3: String) =>
    val sparkle1 = Sparkle(value1)
    val sparkle2 = Sparkle(value2)
    val sparkle3 = Sparkle(value3)

    sparkle1.flatMap(x => sparkle2).flatMap(y => sparkle3)(sparkApp) must_== sparkle1.flatMap(x => sparkle2.flatMap(y => sparkle3))(sparkApp)
  }
}
