import AssemblyKeys._

name := "wookie-sqlserver"

description := "Spark SQL server"

scalaVersion := "2.10.5"

assemblyOption in assembly ~= { _.copy(includeScala = false) }

libraryDependencies ++= spark ++ Seq(sparkThriftServer, scalazStream, psqlJdbc, sparkCsv)

dependencyOverrides +=  "org.apache.avro" % "avro-mapred" % "1.7.5"

net.virtualvoid.sbt.graph.Plugin.graphSettings

mergeStrategy in assembly := {
  case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
  case "log4j.properties" => MergeStrategy.discard
  case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
  case _ => MergeStrategy.first
}

EclipseKeys.withSource := true

EclipseKeys.withBundledScalaContainers := false
