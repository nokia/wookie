import AssemblyKeys._


name := "wookie-sparkapp"

description := "basic helper to run spark apps"

scalaVersion := "2.10.5"

libraryDependencies ++= sparkProvided ++ Seq(spire, esAnalytics, cassandraAnalytics, scallop, scalazCore, shapeless, argonaut, simplelatlng)

assemblyOption in assembly ~= { _.copy(includeScala = false) }

net.virtualvoid.sbt.graph.Plugin.graphSettings

EclipseKeys.withSource := true

EclipseKeys.withBundledScalaContainers := false
