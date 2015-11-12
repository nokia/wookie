organization in ThisBuild := "org.wookie"

name := "wookie"

version in ThisBuild := "0.1.0"

crossScalaVersions in ThisBuild := Seq("2.10.6", "2.11.7")

description := "A minimal library for building data products"

homepage in ThisBuild := Some(url("https://github.com/elyast/wookie"))

startYear in ThisBuild := Some(2015)

licenses in ThisBuild := Seq(
  "Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

scmInfo in ThisBuild := {
  val base = "github.com/elyast/wookie"
  Some(ScmInfo(url(s"https://$base"), s"scm:git:https://$base", Some(s"scm:git:git@$base")))
}

val JvmTarget = "1.7"

pomExtra in ThisBuild :=
  <developers>
    <developer>
      <id>elyast</id>
      <name>Lukasz Jastrzebski</name>
      <email>lukasz.jastrzebski@gmail.com</email>
    </developer>
  </developers>

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
  s"-target:jvm-$JvmTarget",
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
  "Spark Packages Repo" at "http://dl.bintray.com/spark-packages/maven",
  "Elyast Repository" at "https://dl.dropboxusercontent.com/u/16106115/maven2/",
  "Cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos",  
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
  Resolver.url("supler ivy resolver", url("http://dl.bintray.com/elyast/maven"))(Resolver.ivyStylePatterns))

libraryDependencies in ThisBuild ++= specs2

publishMavenStyle in ThisBuild := false

publishArtifact in (ThisBuild, Test) := false

evictionWarningOptions in (ThisBuild, update) := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
