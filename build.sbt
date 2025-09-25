ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.7.3"

libraryDependencies += "com.optimizely.ab" % "core-api" % "4.2.2"
libraryDependencies += "com.optimizely.ab" % "core-httpclient-impl" % "4.2.2"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "2.0.17"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.19.2"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-annotations" % "2.19.2"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.19.2"

lazy val root = (project in file("."))
  .settings(
    name := "optimizely-scala-poc"
  )
