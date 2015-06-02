name := "wookie-app"

description := "basic framework constructs for running micro service"

libraryDependencies ++= http4s ++ logging ++ Seq(kafka, httpClient, scallop)
