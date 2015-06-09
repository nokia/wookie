import AssemblyKeys._

name := "wookie-sparkapp"

description := "basic helper to run spark apps"

scalaVersion := "2.10.5"

libraryDependencies ++= Seq(spire, esAnalytics, cassandraAnalytics, scallop, scalazCore, shapeless, argonaut, simplelatlng) ++ spark

assemblyOption in assembly ~= { _.copy(includeScala = false) }

net.virtualvoid.sbt.graph.Plugin.graphSettings
