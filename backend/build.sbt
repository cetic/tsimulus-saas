import Dependencies.Version

organization:= "be.cetic"

name := "tsimulus-saas-backend"

version:= "0.1"

libraryDependencies := Seq(

  "com.github.scopt" %% "scopt" % "3.5.0",
  "com.github.nscala-time" % "nscala-time_2.11" % "2.12.0",
  "org.apache.commons" % "commons-math3" % "3.6.1",
  "io.spray" %%  "spray-json" % "1.3.2",
  "org.scalactic" %% "scalactic" % "3.0.0",
  "be.cetic" %% "tsimulus" % "0.1.14",
  "org.apache.kafka" %% "kafka" % "0.8.2.2",
  "org.apache.kafka" % "kafka-clients" % "0.8.2.2",
  "org.apache.logging.log4j" % "log4j-core" % "2.7",
  "com.typesafe.akka" %% "akka-actor" % Version.akka,
  "com.typesafe.akka" %% "akka-cluster" % Version.akka,
  "com.typesafe.akka" %% "akka-cluster-metrics" % Version.akka,
  "com.typesafe.akka" %% "akka-slf4j" % Version.akka,
  "com.typesafe.akka" %% "akka-remote" % Version.akka,
  "com.typesafe.akka" %% "akka-stream" %  "2.5.13",
  "com.google.guava" % "guava" % "18.0"
)
