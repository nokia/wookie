import AssemblyKeys._

name := "wookie-spark-twitter"

scalaVersion := "2.10.5"

libraryDependencies ++= sparkProvided ++ Seq(sparkStreamingTwitter)

assemblyOption in assembly ~= { _.copy(includeScala = false) }

mergeStrategy in assembly := {
  case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
  case "log4j.properties" => MergeStrategy.discard
  case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
  case _ => MergeStrategy.first
}
