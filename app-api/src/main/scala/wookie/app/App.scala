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
package wookie.app

import org.rogach.scallop.ScallopConf

abstract class App[A <: ScallopConf](options: Array[String] => A) {

  def run(opt: A): Unit

  final def main(args: Array[String]): Unit = {
    val opt = options(args)
    opt.afterInit()
    opt.assertVerified()
    run(opt)
  }
}
