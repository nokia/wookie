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
package wookie.app.cli

import org.rogach.scallop.ValueConverter
import org.rogach.scallop.ArgType

object URLQueryConverter extends ValueConverter[Map[String, String]] {

  def parseParam(queryParam: String): Option[(String, String)] = {
    val queryEntries = queryParam.split("=", -1)
    val key = queryEntries(0)
    val value = queryEntries.drop(1).mkString("=")
    Some(key -> value)
  }

  def parse(s: List[(String, List[String])]): Either[String, Option[Map[String, String]]] = {
    val queryMap = for {
      elem <- s
      singleElem <- elem._2
      queryProp <- singleElem.split("&", -1)
      p <- parseParam(queryProp) if !queryProp.trim.isEmpty
    } yield {
      p
    }
    Right(Some(queryMap.toMap))
  }

  val tag = scala.reflect.runtime.universe.typeTag[Map[String, String]]
  val argType: ArgType.V = org.rogach.scallop.ArgType.SINGLE
}
