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

import com.twitter.algebird.Monad
import org.apache.spark.streaming.StreamingContext

sealed trait StreamingSparkle[+A] {
  def run(ctx: StreamingContext): A
  def map[B](f: A => B): StreamingSparkle[B] = StreamingSparkle.monad.map(this)(f)
  def flatMap[B](f: A => StreamingSparkle[B]): StreamingSparkle[B] = StreamingSparkle.monad.flatMap(this)(f)
}

object StreamingSparkle {
  def apply[A](f: StreamingContext => A): StreamingSparkle[A] = new StreamingSparkle[A] {
    override def run(ctx: StreamingContext): A = f(ctx)
  }

  implicit val monad = new Monad[StreamingSparkle] {
    override def flatMap[A, B](fa: StreamingSparkle[A])(f: A => StreamingSparkle[B]): StreamingSparkle[B] =
      StreamingSparkle[B] {
        (ctx) => f(fa.run(ctx)).run(ctx)
      }
    override def apply[A](v: A): StreamingSparkle[A] = StreamingSparkle[A](_ => v)
  }
}
