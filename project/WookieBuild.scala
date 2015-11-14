import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object WookieBuild extends Build {

  lazy val app = wookieProject("app-api").
    settings(
      libraryDependencies ++= logging ++ Seq(scallop))

  lazy val web = wookieProject("web-api").
    dependsOn(app).
    settings(
      libraryDependencies ++= http4s)

  lazy val collector = wookieProject("collector-api").
    dependsOn(app).
    settings(
      libraryDependencies ++= Seq(kafka, httpClient, scalazStream, http4sCore, argonaut))

  lazy val sparkApi = wookieProject("spark-api").
    settings(
      libraryDependencies ++= sparkProvided ++ Seq(scallop, spire, shapeless, argonaut, simplelatlng))

  lazy val twitterApi = wookieProject("spark-api-twitter").
    dependsOn(sparkApi).
    settings(
      libraryDependencies ++= sparkProvided ++ Seq(sparkStreamingTwitter))

  lazy val kafkaApi = wookieProject("spark-api-kafka").
    dependsOn(sparkApi).
    settings(
      libraryDependencies ++= sparkProvided ++ Seq(sparkStreamingKafka))

  lazy val oracle = wookieProject("oracle").
    dependsOn(web).
    settings(assembling).
    settings(libraryDependencies ++= sparkMLlib).
    settings(addArtifact(Artifact("wookie-oracle", "assembly"), sbtassembly.Plugin.AssemblyKeys.assembly))

  lazy val pumper = wookieProject("pumper").
    dependsOn(web).
    settings(assembling).
    settings(addArtifact(Artifact("wookie-pumper", "assembly"), sbtassembly.Plugin.AssemblyKeys.assembly))

  lazy val sqlserver = wookieProject("sqlserver").dependsOn(sparkApi).
    settings(
      crossScalaVersions := Seq("2.10.6"),
      libraryDependencies ++= sparkProvided ++ sparkThriftServerProvided ++ Seq(scalazStream, psqlJdbc, sparkCsv,
        cassandraAnalytics, esAnalytics),
      dependencyOverrides +=  "org.apache.avro" % "avro-mapred" % "1.7.5").
    settings(assembling ++ noscala).
    settings(addArtifact(Artifact("wookie-sqlserver", "assembly"), sbtassembly.Plugin.AssemblyKeys.assembly))

  lazy val yqlCollector = wookieExampleProject("yql-collector", "yql-app/collector").
    dependsOn(collector).
    settings(assembling).
    settings(packagedArtifacts := Map.empty)

  lazy val yqlAnalytics = wookieExampleProject("yql-analytics", "yql-app/analytics").
    dependsOn(twitterApi, kafkaApi).
    settings(assembling ++ noscala).
    settings(libraryDependencies ++= sparkProvided).
    settings(packagedArtifacts := Map.empty)

  lazy val yqlVis = wookieExampleProject("yql-vis", "yql-app/visualization").
    dependsOn(web).
    settings(assembling).
    settings(packagedArtifacts := Map.empty)

  lazy val fakeYqlAnalytics = wookieExampleProject("yql-analytics-classpath", "fake/yql-app-analytics").
    dependsOn(yqlAnalytics).
    settings(
      libraryDependencies ++= Seq("org.slf4j" % "slf4j-log4j12" % slf4jVersion) ++ spark,
      packagedArtifacts := Map.empty)

  lazy val fakeSqlserver = wookieExampleProject("sqlserver-classpath", "fake/sqlserver").
    dependsOn(sqlserver).
    settings(
      crossScalaVersions := Seq("2.10.6"),
      libraryDependencies ++= spark ++ sparkThriftServer,
      packagedArtifacts := Map.empty)

  lazy val noscala = Seq(assemblyOption in assembly ~= { _.copy(includeScala = false) })
  lazy val assembling = assemblySettings ++ Seq(mergeStrategy in assembly := {
      case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
      case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
      case "log4j.properties" => MergeStrategy.discard
      case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
      case _ => MergeStrategy.first
    },
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false))

  def wookieProject(name: String) = Project(name, file(name))
    .settings(
      moduleName := s"wookie-$name")

  def wookieExampleProject(name: String, filePath: String) = Project(name, file(filePath))
    .settings(
      moduleName := s"wookie-examples-$name"
    )

  lazy val http4sversion = "0.8.6"
  lazy val http4s = Seq(
      "org.http4s" %% "http4s-blazeclient" % http4sversion,
      "org.http4s" %% "http4s-blazeserver" % http4sversion,
      "org.http4s" %% "http4s-argonaut" % http4sversion,
      "org.http4s" %% "http4s-dsl" % http4sversion,
      "org.http4s" %% "http4s-server" % http4sversion)

  lazy val http4sCore = "org.http4s" %% "http4s-core" % http4sversion

  lazy val http4sDsl = "org.http4s" %% "http4s-dsl" % http4sversion

  lazy val specs2version = "2.4.17"
  lazy val specs2 = Seq(
      "org.specs2" %% "specs2-core" % specs2version  % "test",
      "org.specs2" %% "specs2-scalacheck" % specs2version % "test",
      "org.specs2" %% "specs2-mock" % specs2version % "test",
      "org.specs2" %% "specs2-junit" % specs2version % "test")

  lazy val slf4jVersion = "1.7.12"
  lazy val logging = Seq(
      "org.log4s" %% "log4s" % "1.1.5",
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "org.slf4j" % "log4j-over-slf4j" % slf4jVersion,
      "org.slf4j" % "jul-to-slf4j" % slf4jVersion,
      "org.slf4j" % "jcl-over-slf4j" % slf4jVersion,
      "ch.qos.logback" % "logback-classic" % "1.1.3")

  lazy val simplelatlng = "com.javadocmd" % "simplelatlng" % "1.3.0"
  lazy val argonaut = "io.argonaut" %% "argonaut" % "6.1"
  lazy val scalazVersion = "7.1.2"
  lazy val scalazCore = "org.scalaz" %% "scalaz-core" % scalazVersion
  lazy val scalazEffect = "org.scalaz" %% "scalaz-effect" % scalazVersion
  lazy val scalazConcurrent = "org.scalaz" %% "scalaz-concurrent" % scalazVersion

  lazy val scalazStream = "org.scalaz.stream" %% "scalaz-stream" % "0.7.1a"
  lazy val psqlJdbc = "org.postgresql" % "postgresql" % "9.4-1201-jdbc41"

  lazy val shapeless = "com.chuusai" %% "shapeless" % "2.2.2"
  lazy val scallop = "org.rogach" %% "scallop" % "0.9.5"
  lazy val httpClient = "com.ning" % "async-http-client" % "1.9.27"
  lazy val kafka = ("org.apache.kafka" % "kafka-clients" % "0.8.2.1").
    exclude("log4j", "log4j").
    exclude("org.slf4j", "slf4j-log4j12")

  lazy val cassandraVersion = "2.2.0-rc3"
  lazy val cassandra = Seq(
    "com.datastax.cassandra" % "cassandra-driver-mapping" % cassandraVersion,
    "com.datastax.cassandra"  % "cassandra-driver-core" % cassandraVersion)

  lazy val esAnalytics = ("org.elasticsearch" %% "elasticsearch-spark" % "2.2.0-m1").
    exclude("org.apache.spark", "spark-core_" + "2.10").
    exclude("org.apache.spark", "spark-core_" + "2.11").
    exclude("org.apache.spark", "spark-sql_" + "2.10").
    exclude("org.apache.spark", "spark-sql_" + "2.11").
    exclude("org.apache.spark", "spark-catalyst_" + "2.10").
    exclude("org.apache.spark", "spark-catalyst_" + "2.11")
  lazy val cassandraAnalytics = ("com.datastax.spark" %% "spark-cassandra-connector" % "1.5.0-M2").
    exclude("org.apache.spark", "spark-core_" + "2.10").
    exclude("org.apache.spark", "spark-core_" + "2.11").
    exclude("org.apache.spark", "spark-sql_" + "2.10").
    exclude("org.apache.spark", "spark-sql_" + "2.11").
    exclude("org.apache.spark", "spark-catalyst_" + "2.10").
    exclude("org.apache.spark", "spark-catalyst_" + "2.11")

  lazy val spire = "org.spire-math" %% "spire" % "0.10.1"

  lazy val sparkVersion = "1.5.1"
  lazy val hadoopVersion = "2.6.0-cdh5.4.7"
  lazy val sparkMLlib = Seq(
    ("org.apache.spark" %% "spark-mllib" % sparkVersion).
      exclude("org.apache.hadoop", "hadoop-client").
      exclude("org.slf4j", "slf4j-log4j12"),
    ("org.apache.hadoop" % "hadoop-client" % hadoopVersion).
      exclude("org.slf4j", "slf4j-log4j12").
      exclude("javax.servlet", "servlet-api"))

  lazy val sparkThriftServer = Seq(
    ("org.apache.spark" %% "spark-hive-thriftserver" % sparkVersion).
      exclude("org.mortbay.jetty", "servlet-api").
      exclude("javax.servlet", "servlet-api").
      exclude("org.apache.hadoop", "hadoop-client").
      exclude("org.slf4j", "slf4j-log4j12"),
    ("org.apache.hadoop" % "hadoop-client" % hadoopVersion).
      exclude("org.slf4j", "slf4j-log4j12").
      exclude("javax.servlet", "servlet-api"))

  lazy val sparkStreamingKafka =  "org.apache.spark" %% "spark-streaming-kafka-assembly" % sparkVersion

  lazy val sparkStreamingTwitter =  ("org.apache.spark" %% "spark-streaming-twitter" % sparkVersion).
      exclude("org.apache.hadoop", "hadoop-client").
      exclude("org.slf4j", "slf4j-log4j12").
      exclude("org.apache.spark", "spark-core_" + "2.10").
      exclude("org.apache.spark", "spark-core_" + "2.11").
      exclude("org.apache.spark", "spark-streaming_" + "2.10").
      exclude("org.apache.spark", "spark-streaming_" + "2.11")

  lazy val sparkThriftServerProvided = sparkThriftServer.map(a => a % "provided")
  lazy val spark = Seq(
    ("org.apache.spark" %% "spark-core" % sparkVersion).exclude("org.apache.hadoop", "hadoop-client"),
    ("org.apache.hadoop" % "hadoop-client" % hadoopVersion).exclude("javax.servlet", "servlet-api"),
    "org.apache.spark" %% "spark-streaming" % sparkVersion,
    ("org.apache.spark" %% "spark-hive" % sparkVersion).exclude("org.mortbay.jetty", "servlet-api"),
    "org.apache.spark" %% "spark-mllib" % sparkVersion,
    "org.apache.spark" %% "spark-graphx" % sparkVersion)

  lazy val sparkProvided = spark.map(a => a % "provided")

  lazy val sparkCsv = ("com.databricks" %% "spark-csv" % "1.2.0").
    exclude("org.apache.spark", "spark-core_" + "2.10").
    exclude("org.apache.spark", "spark-core_" + "2.11").
    exclude("org.apache.spark", "spark-sql_" + "2.10").
    exclude("org.apache.spark", "spark-sql_" + "2.11").
    exclude("org.apache.spark", "spark-catalyst_" + "2.10").
    exclude("org.apache.spark", "spark-catalyst_" + "2.11")
  lazy val sparkIndexedRdd = ("amplab" % "spark-indexedrdd" % "0.3").
    exclude("org.apache.spark", "spark-core_" + "2.10").
    exclude("org.apache.spark", "spark-core_" + "2.11").
    exclude("org.apache.spark", "spark-sql_" + "2.10").
    exclude("org.apache.spark", "spark-sql_" + "2.11").
    exclude("org.apache.spark", "spark-catalyst_" + "2.10").
    exclude("org.apache.spark", "spark-catalyst_" + "2.11")

}
