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

package wookie.spark

import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.StreamingContext
import org.junit.runner.RunWith
import org.specs2.ScalaCheck
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SparkleSpec extends Specification with Mockito with ScalaCheck {

  val sparkApp = mock[SparkSession]
  val streamingApp = mock[StreamingContext]

  "Identity Law in Sparkle" >> prop { (value: String) =>
    val sparkle = Sparkle(_ => value)
    val mapped = sparkle.map(identity)
    mapped.run(sparkApp) must_== sparkle.run(sparkApp)
  }

  "Identity Law in StreamingSparkle" >> prop { (value: String) =>
    val sparkle = StreamingSparkle(_ => value)
    val mapped = sparkle.map(identity)
    mapped.run(streamingApp) must_== sparkle.run(streamingApp)
  }

  "Left identity" >> prop { (value1: String, value2: String) =>
    val sparkle1 = Sparkle(_ => value1)
    val sparkle2 = Sparkle(_ => value2)
    sparkle1.flatMap(x => sparkle2).run(sparkApp) must_== sparkle2.run(sparkApp)
  }

  "Right identity" >> prop { (value1: String) =>
    val sparkle1 = Sparkle(_ => value1)
    sparkle1.flatMap(x => Sparkle(_ => x)).run(sparkApp) must_== sparkle1.run(sparkApp)
  }

  "Associativity"  >> prop { (value1: String, value2: String, value3: String) =>
    val sparkle1 = Sparkle(_ => value1)
    val sparkle2 = Sparkle(_ => value2)
    val sparkle3 = Sparkle(_ => value3)

    sparkle1.flatMap(x => sparkle2).flatMap(y => sparkle3).run(sparkApp) must_== sparkle1.flatMap(x => sparkle2.flatMap(y => sparkle3)).run(sparkApp)
  }
}
