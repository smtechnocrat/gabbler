organization := "name.heikoseeberger"

name := "gabbler"

version := "1.1.0"

scalaVersion := Version.scala

// TODO Remove as soon as spray-json is on Maven Cental
resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= Dependencies.gabbler

scalacOptions ++= List(
  "-unchecked",
  "-deprecation",
  "-language:_",
  "-target:jvm-1.6",
  "-encoding", "UTF-8"
)

// initialCommands in console := "import name.heikoseeberger.gabbler._"
