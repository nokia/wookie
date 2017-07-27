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
import sbt._
import Keys._
import sbtassembly.AssemblyPlugin.autoImport._

object WookieBuild extends Build {

  lazy val app = wookieProject("app-api").
    settings(
      libraryDependencies ++= fullLogging ++ Seq(scallop, scalazCore))

  lazy val collector = wookieProject("collector-api").
    dependsOn(app).
    settings(
      libraryDependencies ++= Seq(kafka, scalazStream, http4sClient, http4sDsl, http4sArgonaut))

  lazy val sparkApi = wookieProject("spark-api").
    settings(
      libraryDependencies ++= sparkProvided ++ Seq(scallop, shapeless, log4s, sparkTesting, algebird)).
    settings(sparkTestingSettings)

  lazy val kafkaApi = wookieProject("spark-api-kafka").
    dependsOn(sparkApi).
    settings(
      libraryDependencies ++= sparkProvided ++ Seq(sparkStreamingKafka))

  lazy val sqlserver = wookieProject("sqlserver").dependsOn(sparkApi).
    settings(
      libraryDependencies ++= sparkProvided ++ sparkThriftServerProvided ++ Seq(scalazStream, cassandraAnalytics) ++ hadoopAws ,
      dependencyOverrides +=  "org.apache.avro" % "avro-mapred" % "1.7.5")

  lazy val examples = wookieProject("examples").
    dependsOn(collector, kafkaApi).
    settings(assembling).
    settings(libraryDependencies ++= sparkProvided ++ Seq(simplelatlng, sparkStreamingTwitter))

  lazy val fakeSqlserver = wookieProject("sqlserver-classpath").
    dependsOn(sqlserver).
    settings(
      libraryDependencies ++= spark ++ sparkThriftServer)

  lazy val fakeExamples = wookieProject("examples-classpath").
    dependsOn(examples).
    settings(
      libraryDependencies ++= spark)

  lazy val noscala = Seq(assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false))
  lazy val assembling = Seq(assemblyMergeStrategy in assembly := {
      case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
      case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
      case "log4j.properties" => MergeStrategy.discard
      case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
      case _ => MergeStrategy.first
    },
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false))

  lazy val sparkTestingSettings = Seq(parallelExecution in Test := false)

  def wookieProject(name: String): Project = Project(name, file(name))
    .settings(
      moduleName := s"wookie-$name")

  lazy val sparkTesting = "com.holdenkarau" %% "spark-testing-base" % "2.2.0_0.7.2" % "test"
  lazy val http4sversion = "0.14.1a"
  lazy val http4sClient = "org.http4s" %% "http4s-blaze-client" % http4sversion
  lazy val http4sDsl = "org.http4s" %% "http4s-dsl" % http4sversion
  lazy val http4sArgonaut = "org.http4s" %% "http4s-argonaut" % http4sversion

  lazy val specs2version = "3.7.2"
  lazy val specs2 = Seq(
      "org.specs2" %% "specs2-core" % specs2version  % "test",
      "org.specs2" %% "specs2-scalacheck" % specs2version % "test",
      "org.specs2" %% "specs2-matcher-extra" % specs2version % "test",
      "org.specs2" %% "specs2-matcher" % specs2version % "test",
      "org.specs2" %% "specs2-mock" % specs2version % "test",
      "org.specs2" %% "specs2-junit" % specs2version % "test")

  lazy val log4s = "org.log4s" %% "log4s" % "1.3.0"
  lazy val slf4jVersion = "1.7.21"
  lazy val fullLogging = Seq(
      log4s,
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "org.slf4j" % "log4j-over-slf4j" % slf4jVersion,
      "org.slf4j" % "jul-to-slf4j" % slf4jVersion,
      "org.slf4j" % "jcl-over-slf4j" % slf4jVersion,
      "ch.qos.logback" % "logback-classic" % "1.1.7")

  lazy val argonaut = "io.argonaut" %% "argonaut" % "6.2-M3"
  lazy val scalazVersion = "7.2.0"
  lazy val scalazCore = "org.scalaz" %% "scalaz-core" % scalazVersion
  lazy val scalazEffect = "org.scalaz" %% "scalaz-effect" % scalazVersion
  lazy val scalazConcurrent = "org.scalaz" %% "scalaz-concurrent" % scalazVersion
  lazy val shapeless = "com.chuusai" %% "shapeless" % "2.3.0"
  lazy val scalazStream = "org.scalaz.stream" %% "scalaz-stream" % "0.8a"

  lazy val scallop = "org.rogach" %% "scallop" % "0.9.5"
  lazy val kafka = ("org.apache.kafka" % "kafka-clients" % "0.8.2.1").
    exclude("log4j", "log4j").
    exclude("org.slf4j", "slf4j-log4j12")

  lazy val cassandraAnalytics = ("com.datastax.spark" %% "spark-cassandra-connector" % "2.0.3").
    exclude("org.apache.spark", "spark-core_" + "2.11").
    exclude("org.apache.spark", "spark-sql_" + "2.11").
    exclude("org.apache.spark", "spark-catalyst_" + "2.11")

  lazy val sparkVersion = "2.2.0"

  lazy val sparkThriftServer = Seq(
    ("org.apache.spark" %% "spark-hive-thriftserver" % sparkVersion).exclude("org.slf4j", "slf4j-log4j12"))

  lazy val sparkStreamingKafka =  ("org.apache.spark" %% "spark-streaming-kafka-0-8-assembly" % sparkVersion).exclude("org.slf4j", "slf4j-log4j12")

  lazy val sparkThriftServerProvided = sparkThriftServer.map(a => a % "provided")
  lazy val spark = Seq(
    ("org.apache.spark" %% "spark-streaming" % sparkVersion).exclude("org.slf4j", "slf4j-log4j12"),
    ("org.apache.spark" %% "spark-sql" % sparkVersion).exclude("org.slf4j", "slf4j-log4j12"))

  lazy val sparkProvided = spark.map(a => a % "provided")

  lazy val algebird = "com.twitter" %% "algebird-spark" % "0.12.3"

  lazy val awsSdkVersion = "1.10.20"

  lazy val hadoopVersion = "2.6.0-cdh5.7.1"

  lazy val hadoopAws = Seq("org.apache.hadoop" % "hadoop-aws" % hadoopVersion intransitive(),
    ("com.amazonaws" % "aws-java-sdk-s3" % awsSdkVersion).
      exclude("com.fasterxml.jackson.core", "jackson-databind").
      exclude("commons-logging", "commons-logging")
  )

  lazy val simplelatlng = "com.javadocmd" % "simplelatlng" % "1.3.0"

  lazy val sparkStreamingTwitter =  ("org.apache.bahir" %% "spark-streaming-twitter" % "2.1.1").
    exclude("org.apache.hadoop", "hadoop-client").
    exclude("org.slf4j", "slf4j-log4j12").
    exclude("org.apache.spark", "spark-core_" + "2.10").
    exclude("org.apache.spark", "spark-core_" + "2.11").
    exclude("org.apache.spark", "spark-streaming_" + "2.10").
    exclude("org.apache.spark", "spark-streaming_" + "2.11")
}
