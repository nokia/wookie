import AssemblyKeys._

name := "wookie-sqlserver"

description := "Spark SQL server"

scalaVersion := "2.10.5"

assemblyOption in assembly ~= { _.copy(includeScala = false) }

libraryDependencies ++= spark ++ Seq(sparkThriftServer, scalazStream, psqlJdbc, sparkCsv)

dependencyOverrides +=  "org.apache.avro" % "avro-mapred" % "1.7.5"

net.virtualvoid.sbt.graph.Plugin.graphSettings