import sbt._
import Keys._

object WookieBuild extends Build {

  lazy val http4sversion = "0.6.5"
  lazy val http4s = Seq(
      "org.http4s" %% "http4s-blazeclient" % http4sversion,
      "org.http4s" %% "http4s-blazeserver" % http4sversion,
      "org.http4s" %% "http4s-argonaut" % http4sversion,
      "org.http4s" %% "http4s-dsl" % http4sversion,
      "org.http4s" %% "http4s-server" % http4sversion,
      "org.http4s" %% "rho-swagger" % "0.4.0")

  lazy val specs2version = "2.4.15" 
  lazy val specs2 = Seq(
      "org.specs2" %% "specs2-core" % specs2version  % "test",
      "org.specs2" %% "specs2-scalacheck" % specs2version % "test",
      "org.specs2" %% "specs2-mock" % specs2version % "test",
      "org.specs2" %% "specs2-junit" % specs2version % "test")

  lazy val logging = Seq(
      "org.log4s" %% "log4s" % "1.1.3",
      "org.slf4j" % "slf4j-api" % "1.7.10",
      "org.slf4j" % "log4j-over-slf4j" % "1.7.10",
      "ch.qos.logback" % "logback-classic" % "1.0.13")

  lazy val scalazVersion = "7.1.1"
  lazy val scalazCore = "org.scalaz" %% "scalaz-core" % scalazVersion
  lazy val scalazEffect = "org.scalaz" %% "scalaz-effect" % scalazVersion
  lazy val scalazConcurrent = "org.scalaz" %% "scalaz-concurrent" % scalazVersion

  lazy val scalazStream = "org.scalaz.stream" %% "scalaz-stream" % "0.7a"
  lazy val psqlJdbc = "org.postgresql" % "postgresql" % "9.4-1201-jdbc41"
  
  lazy val shapeless = "com.chuusai" %% "shapeless" % "2.2.0"  
  lazy val scallop = "org.rogach" %% "scallop" % "0.9.5"
  lazy val redisClient = "redis.clients" % "jedis" % "2.6.0"
  lazy val esClient = "io.searchbox" % "jest" % "0.1.3"
  lazy val httpClient = "com.ning" % "async-http-client" % "1.9.11"
  lazy val kafka = ("org.apache.kafka" % "kafka-clients" % "0.8.2.0").exclude("log4j", "log4j").exclude("org.slf4j", "slf4j-log4j12")
  lazy val cassandraVersion = "2.1.5"
  lazy val cassandra = Seq(
    "com.datastax.cassandra" % "cassandra-driver-mapping" % cassandraVersion,
    "com.datastax.cassandra"  % "cassandra-driver-core" % cassandraVersion)  

  lazy val esAnalytics = ("org.elasticsearch" %% "elasticsearch-spark" % "2.1.0.Beta4").
    exclude("org.apache.spark", "spark-core_2.10").
    exclude("org.apache.spark", "spark-sql_2.10")
  lazy val cassandraAnalytics = ("com.datastax.spark" %% "spark-cassandra-connector" % "1.3.0-SNAPSHOT").
    exclude("org.apache.spark", "spark-core_2.10").
    exclude("org.apache.spark", "spark-sql_2.10")
  lazy val spire = "org.spire-math" %% "spire" % "0.9.1"  

  lazy val sparkVersion = "1.3.1"
  lazy val sparkMLlib = "org.apache.spark" %% "spark-mllib" % sparkVersion
  lazy val sparkThriftServer = "org.apache.spark" %% "spark-hive-thriftserver" % sparkVersion
  lazy val spark = Seq(
    ("org.apache.spark" %% "spark-core" % sparkVersion).exclude("org.apache.hadoop", "hadoop-client") % "provided",
    "org.apache.hadoop" % "hadoop-client" % "2.5.0-cdh5.2.0" % "provided",
    "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided",
    "org.apache.spark" %% "spark-hive" % sparkVersion % "provided",
    sparkMLlib % "provided",
    "org.apache.spark" %% "spark-graphx" % sparkVersion % "provided",
    "org.apache.spark" %% "spark-streaming-kafka" % sparkVersion % "provided",
    "org.apache.spark" %% "spark-streaming-twitter" % sparkVersion % "provided")

  lazy val sparkCsv = "com.databricks" %% "spark-csv" % "1.0.3"
  lazy val sparkIndexedRdd = "amplab" %% "spark-indexedrdd" % "0.1"
  
}
