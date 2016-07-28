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
organization in ThisBuild := "org.wookie"

name := "wookie"

version in ThisBuild := "1.0.0"

scalaVersion in ThisBuild := "2.11.8"

description := "A minimal library for building data products"

homepage in ThisBuild := Some(url("https://github.com/elyast/wookie"))

licenses in ThisBuild := Seq(
  "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

scmInfo in ThisBuild := {
  val base = "github.com/elyast/wookie"
  Some(ScmInfo(url(s"https://$base"), s"scm:git:https://$base", Some(s"scm:git:git@$base")))
}

val JvmTarget = "1.8"

pomExtra in ThisBuild :=
  <developers>
    <developer>
      <id>elyast</id>
      <name>Lukasz Jastrzebski</name>
      <email>lukasz.jastrzebski@gmail.com</email>
    </developer>
  </developers>

scalacOptions in ThisBuild ++= Seq(
  "-deprecation",
  "-feature",
  "-language:existentials",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-Xfatal-warnings",
  "-Xfuture",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  s"-target:jvm-$JvmTarget",
  "-unchecked",
  "-Xlint")

javacOptions in ThisBuild ++= Seq(
  "-source", JvmTarget,
  "-target", JvmTarget,
  "-Xlint:deprecation",
  "-Xlint:unchecked")

resolvers in ThisBuild ++= Seq(
  Resolver.typesafeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  "Sonatype OSS Releases"  at "http://oss.sonatype.org/content/repositories/releases/",
  "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "Akka Repository" at "http://repo.akka.io/releases/",
  "Spark Packages Repo" at "http://dl.bintray.com/spark-packages/maven",
  "Cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
  Resolver.url("supler ivy resolver", url("http://dl.bintray.com/elyast/maven"))(Resolver.ivyStylePatterns))

libraryDependencies in ThisBuild ++= specs2

publishMavenStyle in ThisBuild := true

publishArtifact in (ThisBuild, Test) := false

packagedArtifacts in file(".") := Map.empty

evictionWarningOptions in (ThisBuild, update) := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)

