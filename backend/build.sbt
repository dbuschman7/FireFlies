name := """backend"""

version := "1.0-SNAPSHOT"

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

startYear := Some(2015)

description := "Backend App for Firefly cataloging and processing"

scalaVersion := "2.11.7"

developers := List(Developer("lightspeed7","Dave Buschman","dbuschman7@gmail.com",url("https://www.linkedin.com/in/dbuschman7/")))

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq(
  specs2 % Test,
  "com.softwaremill.macwire" %% "macros" % "1.0.5",
  "com.softwaremill.macwire" %% "runtime" % "1.0.5",
  "org.reactivemongo" %% "reactivemongo" % "0.11.5",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.5.play24",
  
  "org.scalatest" %% "scalatest" % "2.2.5" % Test,
  "org.scalacheck" %% "scalacheck" % "1.12.2" % Test,
  "org.scalatestplus" %% "play" % "1.4.0-M4" % Test
)

routesGenerator := play.routes.compiler.InjectedRoutesGenerator

scalariformSettings
