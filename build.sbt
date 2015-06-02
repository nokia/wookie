import AssemblyKeys._

lazy val `app-api` = project.settings(assemblySettings: _*)

lazy val `sparkapp-api` = project.settings(assemblySettings: _*)

lazy val oracle = project.dependsOn(`app-api`).settings(assemblySettings: _*)

lazy val pumper = project.dependsOn(`app-api`).settings(assemblySettings: _*)

lazy val sqlserver = project.dependsOn(`sparkapp-api`).settings(assemblySettings: _*)

lazy val `yql-app-collector` = Project("yql-app-collector", file("yql-app/collector")).dependsOn(`app-api`).settings(assemblySettings: _*)

lazy val `yql-app-analytics` = Project("yql-app-analytics", file("yql-app/analytics")).dependsOn(`sparkapp-api`).settings(assemblySettings: _*)

lazy val `yql-app-visualization` = Project("yql-app-visualization", file("yql-app/visualization")).dependsOn(`app-api`).settings(assemblySettings: _*)


organization in ThisBuild := "org.wookie"

name := "wookie"

version in ThisBuild := "0.0.1-SNAPSHOT"

description := "A minimal, framework for building data products"

homepage in ThisBuild := Some(url("https://github.com/elyast/wookie"))

startYear in ThisBuild := Some(2015)

licenses in ThisBuild := Seq(
  "Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

scmInfo in ThisBuild := {
  val base = "github.com/elyast/wookie"
  Some(ScmInfo(url(s"https://$base"), s"scm:git:https://$base", Some(s"scm:git:git@$base")))
}

pomExtra in ThisBuild := (
  <developers>
    <developer>
      <id>elyast</id>
      <name>Lukasz Jastrzebski</name>
      <email>lukasz.jastrzebski@gmail.com</email>
    </developer>
  </developers>)

scalaVersion in ThisBuild := "2.11.6"

val JvmTarget = "1.7"

scalacOptions in ThisBuild ++= Seq(
  "-deprecation",
  "-feature",
  "-language:existentials",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-Xfatal-warnings",
  "-Xfuture",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  s"-target:jvm-${JvmTarget}",
  "-unchecked",
  "-Xlint")

javacOptions in ThisBuild ++= Seq(
  "-source", JvmTarget,
  "-target", JvmTarget,
  "-Xlint:deprecation",
  "-Xlint:unchecked")

resolvers in ThisBuild ++= Seq(
  Resolver.typesafeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  "Sonatype OSS Releases"  at "http://oss.sonatype.org/content/repositories/releases/",
  "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "Akka Repository" at "http://repo.akka.io/releases/",
  "Elyast Repository" at "https://dl.dropboxusercontent.com/u/16106115/maven2/",
  "Cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos",  
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases")

libraryDependencies in ThisBuild ++= specs2

publishMavenStyle in ThisBuild := true

publishArtifact in (ThisBuild, Test) := false
