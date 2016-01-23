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

import wookie.spark.cli.{SparkApp, SparkStreamingApp}

trait Sparkle[A, APP <: SparkApp[_]] { parent =>
  def apply(app: APP): A

  def flatMap[B](f: A => Sparkle[B, APP]): Sparkle[B, APP] = new Sparkle[B, APP] {
    def apply(app: APP): B = {
      val parentResult: A = parent(app)
      val nextSparkle: Sparkle[B, APP] = f(parentResult)
      nextSparkle(app)
    }
  }

  def map[B](f: A => B): Sparkle[B, APP] = new Sparkle[B, APP] {
    def apply(app: APP): B = {
      f(parent(app))
    }
  }
}

object Sparkle {
  def apply[A](f: => A): Sparkle[A, SparkApp[_]] = new Sparkle[A, SparkApp[_]] {
    def apply(app: SparkApp[_]): A = {
      f
    }
  }
}

object StreamingSparkle {
  def apply[A](f: => A): Sparkle[A, SparkStreamingApp[_]] = new Sparkle[A, SparkStreamingApp[_]] {
    def apply(app: SparkStreamingApp[_]): A = {
      f
    }
  }
}
