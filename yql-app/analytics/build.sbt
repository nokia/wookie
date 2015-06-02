import AssemblyKeys._

name := "yql-analytics"

description := "yahoo API sample analytics"

scalaVersion := "2.10.5"

assemblyOption in assembly ~= { _.copy(includeScala = false) }

libraryDependencies ++= spark

net.virtualvoid.sbt.graph.Plugin.graphSettings
