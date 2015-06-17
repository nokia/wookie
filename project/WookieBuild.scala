import sbt._
import Keys._

object WookieBuild extends Build {

  lazy val http4sversion = "0.7.0"
  lazy val http4s = Seq(
      "org.http4s" %% "http4s-blazeclient" % http4sversion,
      "org.http4s" %% "http4s-blazeserver" % http4sversion,
      "org.http4s" %% "http4s-argonaut" % http4sversion,
      "org.http4s" %% "http4s-dsl" % http4sversion,
      "org.http4s" %% "http4s-server" % http4sversion)

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
  lazy val redisClient = "redis.clients" % "jedis" % "2.7.2"
  lazy val httpClient = "com.ning" % "async-http-client" % "1.9.27"
  lazy val kafka = ("org.apache.kafka" % "kafka-clients" % "0.8.2.1").
    exclude("log4j", "log4j").
    exclude("org.slf4j", "slf4j-log4j12")

  lazy val cassandraVersion = "2.1.5"
  lazy val cassandra = Seq(
    "com.datastax.cassandra" % "cassandra-driver-mapping" % cassandraVersion,
    "com.datastax.cassandra"  % "cassandra-driver-core" % cassandraVersion)  

  lazy val esAnalytics = ("org.elasticsearch" %% "elasticsearch-spark" % "2.1.0.Beta4").
    exclude("org.apache.spark", "spark-core_2.10").
    exclude("org.apache.spark", "spark-sql_2.10").
    exclude("org.apache.spark", "spark-catalyst_2.10")
  lazy val cassandraAnalytics = ("com.datastax.spark" %% "spark-cassandra-connector" % "1.4.0-M1").
    exclude("org.apache.spark", "spark-core_2.10").
    exclude("org.apache.spark", "spark-sql_2.10").
    exclude("org.apache.spark", "spark-catalyst_2.10")
  lazy val spire = "org.spire-math" %% "spire" % "0.10.1"

  lazy val sparkVersion = "1.4.0"
  lazy val hadoopVersion = "2.6.0"
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

  lazy val sparkStreamingKafka =  ("org.apache.spark" %% "spark-streaming-kafka" % sparkVersion).
      exclude("org.apache.hadoop", "hadoop-client").
      exclude("org.slf4j", "slf4j-log4j12").
      exclude("org.apache.spark", "spark-core_2.10").
      exclude("org.apache.spark", "spark-streaming_2.10")

  lazy val sparkStreamingTwitter =  ("org.apache.spark" %% "spark-streaming-twitter" % sparkVersion).
      exclude("org.apache.hadoop", "hadoop-client").
      exclude("org.slf4j", "slf4j-log4j12").
      exclude("org.apache.spark", "spark-core_2.10").
      exclude("org.apache.spark", "spark-streaming_2.10")

  lazy val sparkThriftServerProvided = sparkThriftServer.map(a => a % "provided")
  lazy val spark = Seq(
    ("org.apache.spark" %% "spark-core" % sparkVersion).exclude("org.apache.hadoop", "hadoop-client"),
    ("org.apache.hadoop" % "hadoop-client" % hadoopVersion).exclude("javax.servlet", "servlet-api"),
    "org.apache.spark" %% "spark-streaming" % sparkVersion,
    ("org.apache.spark" %% "spark-hive" % sparkVersion).exclude("org.mortbay.jetty", "servlet-api"),
    "org.apache.spark" %% "spark-mllib" % sparkVersion,
    "org.apache.spark" %% "spark-graphx" % sparkVersion)

  lazy val sparkProvided = spark.map(a => a % "provided")

  lazy val sparkCsv = ("com.databricks" %% "spark-csv" % "1.1.0").
    exclude("org.apache.spark", "spark-core_2.10").
    exclude("org.apache.spark", "spark-sql_2.10").
    exclude("org.apache.spark", "spark-catalyst_2.10")
  lazy val sparkIndexedRdd = ("amplab" % "spark-indexedrdd" % "0.1").
    exclude("org.apache.spark", "spark-core_2.10").
    exclude("org.apache.spark", "spark-sql_2.10").
    exclude("org.apache.spark", "spark-catalyst_2.10")
  
}
