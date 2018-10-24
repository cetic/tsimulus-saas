import Dependencies.Version

name := "tsimulus-saas-backend"

version:= "0.1"


mainClass in(Compile, run) := Some("be.cetic.backend.Backend")
mainClass in(Compile, packageBin) := Some("be.cetic.backend.Backend")

libraryDependencies := Seq(
  "com.github.scopt" %% "scopt" % "3.5.0",
  "io.spray" %%  "spray-json" % "1.3.2",
  "org.scalactic" %% "scalactic" % "3.0.0",
  "be.cetic" %% "tsimulus" % "0.1.14",
  "com.typesafe.akka" %% "akka-actor" % Version.akka,
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.5",
  "com.typesafe.akka" %% "akka-http" % "10.1.5",
  "com.typesafe.akka" %% "akka-stream" %  "2.5.13"
)