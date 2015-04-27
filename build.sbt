name := """TwitterBotPlay"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "org.twitter4j" % "twitter4j-stream" % "4.0.2",
  "org.twitter4j" % "twitter4j-core" % "4.0.3",
  "org.apache.logging.log4j" % "log4j-core" % "2.2",
  "org.apache.logging.log4j" % "log4j-api" % "2.2",
  "junit" % "junit" % "4.4",
  "com.googlecode.json-simple" % "json-simple" % "1.1",
  "org.apache.commons" % "commons-lang3" % "3.3.2"
)