organization := "name.heikoseeberger"

name := "gabbler"

version := "0.1.0"

scalaVersion := Version.scala

libraryDependencies ++= Dependencies.gabbler

scalacOptions ++= List(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-language:_",
  "-target:jvm-1.6",
  "-encoding", "UTF-8"
)

// initialCommands in console := "import name.heikoseeberger.gabbler._"
