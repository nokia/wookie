import AssemblyKeys._

name := "yql-analytics"

description := "yahoo API sample analytics"

scalaVersion := "2.10.5"

libraryDependencies ++= spark

assemblyOption in assembly ~= { _.copy(includeScala = false) }

net.virtualvoid.sbt.graph.Plugin.graphSettings

EclipseKeys.withSource := true

EclipseKeys.withBundledScalaContainers := false
