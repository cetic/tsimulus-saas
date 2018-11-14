import Dependencies.Version

name := "tsimulus-saas-backend"

version:= "0.1"


mainClass in(Compile, run) := Some("be.cetic.tsaas.Backend")
mainClass in(Compile, packageBin) := Some("be.cetic.backend.Backend")

libraryDependencies := Seq(
  "com.github.scopt" %% "scopt" % "3.5.0",
  "io.spray" %%  "spray-json" % "1.3.2",
  "org.scalactic" %% "scalactic" % "3.0.0",
  "be.cetic" %% "tsimulus" % "0.1.14",
  "com.typesafe.akka" %% "akka-actor" % Version.akka,
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.5",
  "com.typesafe.akka" %% "akka-http" % "10.1.5",
  "com.typesafe.akka" %% "akka-stream" %  "2.5.13",
  "be.cetic" %% "rts-gen" % "0.1.13",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.freemarker" % "freemarker" % "2.3.28",
  "javax.ws.rs" % "javax.ws.rs-api" % "2.0.1",
  "ch.megard" %% "akka-http-cors" % "0.3.0"
)