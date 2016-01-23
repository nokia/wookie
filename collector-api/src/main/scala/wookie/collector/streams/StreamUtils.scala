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
package wookie.collector.streams

import scalaz.stream.Process
import scalaz.stream.Process._

/**
  * Created by ljastrze on 11/16/15.
  */
object StreamUtils {

  def once[F[_], R, O](acquire: F[R])(release: R => F[Unit])(step: R => F[O]): Process[F, O] = {
    eval(acquire).flatMap { r =>
      eval(step(r)).onComplete[F, O](eval_(release(r)))
    }
  }

}
