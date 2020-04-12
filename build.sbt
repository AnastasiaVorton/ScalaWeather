name := "Weather"

version := "0.1"

scalaVersion := "2.13.1"

lazy val akkaVersion = "2.6.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.osinka.i18n" %% "scala-i18n" % "1.0.3",
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "com.typesafe.play" %% "play-json" % "2.8.1"
)