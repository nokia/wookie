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
package wookie

import com.twitter.algebird.Monad
import org.apache.spark.sql.SparkSession
import wookie.spark.SparkRuntime

trait RuntimeEnvironment {
  type A
  def get: A
}
/**
  * Spark session execution environment
  * @tparam A
  */
trait Sparkle[+A] {
  def run(ctx: RuntimeEnvironment): A
  def map[B](f: A => B): Sparkle[B] = Sparkle.monad.map(this)(f)
  def flatMap[B](f: A => Sparkle[B]): Sparkle[B] = Sparkle.monad.flatMap(this)(f)
}

object Sparkle {
  def apply[A](f: RuntimeEnvironment => A): Sparkle[A] = new Sparkle[A] {
    override def run(ctx: RuntimeEnvironment): A = f(ctx)
  }

  implicit val monad = new Monad[Sparkle] {
    override def flatMap[A, B](fa: Sparkle[A])(f: A => Sparkle[B]): Sparkle[B] =
      Sparkle[B] {
        (ctx) => f(fa.run(ctx)).run(ctx)
      }
    override def apply[A](v: A): Sparkle[A] = Sparkle[A](_ => v)
  }
}


