import Dependencies.Version

name := "tsimulus-saas-backend"

version:= "0.1"

libraryDependencies := Seq(
  "com.github.scopt" %% "scopt" % "3.5.0",
  "io.spray" %%  "spray-json" % "1.3.2",
  "org.scalactic" %% "scalactic" % "3.0.0",
  "be.cetic" %% "tsimulus" % "0.1.14",
  "org.apache.logging.log4j" % "log4j-core" % "2.7",
  "com.typesafe.akka" %% "akka-actor" % Version.akka,
  "com.typesafe.akka" %% "akka-stream" %  "2.5.13"
)