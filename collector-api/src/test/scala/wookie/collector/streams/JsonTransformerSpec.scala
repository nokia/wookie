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

import argonaut.Argonaut._
import argonaut.{DecodeJson, EncodeJson}
import org.http4s.{ParseException, Response}
import org.junit.runner.RunWith
import org.scalacheck.{Gen, Arbitrary}
import org.specs2.ScalaCheck
import org.specs2.matcher.JsonMatchers
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import scodec.bits.ByteVector

import scalaz.concurrent.Task

/**
  * Created by ljastrze on 11/22/15.
  */
@RunWith(classOf[JUnitRunner])
class JsonTransformerSpec extends Specification with ScalaCheck with JsonMatchers {

  case class Foo(attr6: String)
  case class Bar(attr1: String, attr2: List[String], attr3: Boolean, attr4: Int, attr5: Foo)

  implicit val encoder1: EncodeJson[Foo] = casecodec1(Foo.apply, Foo.unapply)("attr6")
  implicit val decoder1: DecodeJson[Foo] = casecodec1(Foo.apply, Foo.unapply)("attr6")
  implicit val encoder2: EncodeJson[Bar] = casecodec5(Bar.apply, Bar.unapply)("attr1", "attr2", "attr3", "attr4", "attr5")
  implicit val decoder2: DecodeJson[Bar] = casecodec5(Bar.apply, Bar.unapply)("attr1", "attr2", "attr3", "attr4", "attr5")


  def alphaNumList: Arbitrary[List[String]] = {
    Arbitrary(Gen.containerOf[List, String](Gen.identifier))
  }

  def alphaNum: Arbitrary[String] = {
    Arbitrary(Gen.identifier)
  }

  "Correct Json should be decoded" >> prop { (attr1: String, attr2: List[String],
                                              attr3: Boolean, attr4: Int, attr6: String) =>
    val attr2str = if (attr2.isEmpty) "[]" else s"""["${attr2.mkString("\",\"")}"]"""
    val input =
      s"""{"attr1": "$attr1", "attr2": $attr2str, "attr3": $attr3,
         "attr4": $attr4, "attr5": { "attr6": "$attr6" } }"""
    val resp = Response(body=scalaz.stream.Process.eval(Task.now(ByteVector(input.getBytes))))
    val task = JsonTransformer.asObject[Bar](resp)
    val result = task.attemptRun
    result.isRight must_== true
    result.toOption.get must_==Bar(attr1, attr2, attr3, attr4, Foo(attr6))
  }.setArbitrary1(alphaNum).setArbitrary2(alphaNumList).setArbitrary5(alphaNum)


  "Incorrect Json should not be decoded" in {
    val input = """{"random": "fields"}"""
    val resp = Response(body=scalaz.stream.Process.eval(Task.now(ByteVector(input.getBytes))))
    val task = JsonTransformer.asObject[Bar](resp)
    val result = task.attemptRun
    val exception = result.swap.toOption.get
    result.isRight must_== false
    exception.getClass must_== classOf[ParseException]
  }


  "Correct Json should be dumped back to text" >> prop { (attr1: String, attr2: List[String],
                                                          attr3: Boolean, attr4: Int, attr6: String) =>
    val attr2str = if (attr2.isEmpty) "[]" else s"""["${attr2.mkString("\",\"")}"]"""
    val input =
      s"""{"attr1": "$attr1", "attr2": $attr2str, "attr3": $attr3,
         "attr4": $attr4, "attr5": { "attr6": "$attr6" } }"""
    val resp = Response(body=scalaz.stream.Process.eval(Task.now(ByteVector(input.getBytes))))
    val task = JsonTransformer.asText[Bar](resp)
    val result = task.attemptRun
    val resultText = result.toOption.get
    result.isRight must_== true
    resultText must /("attr1" -> attr1)
    resultText must /("attr3" -> attr3)
    resultText must /("attr4" -> attr4)
    resultText must /("attr5") /("attr6" -> attr6)
    resultText must contain(s""""attr2":$attr2str""")
  }.setArbitrary1(alphaNum).setArbitrary2(alphaNumList).setArbitrary5(alphaNum)
}
