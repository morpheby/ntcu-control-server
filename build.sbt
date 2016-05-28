
name := """ntcu-control-server"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  cache,
  ws,
  "com.typesafe.play" %% "play-slick" % "2.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.0",
  "com.h2database" % "h2" % "1.4.191",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

libraryDependencies ++= Seq(
  "com.warrenstrange" % "googleauth" % "1.1.0",
  "com.chuusai" %% "shapeless" % "2.3.1"
)

resolvers += Resolver.sonatypeRepo("public")

libraryDependencies ++= Seq(
  "de.svenkubiak" % "jBCrypt" % "0.4.1"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"


fork in run := true
