import sbt._

object Dependencies {

  object Version {
    val akka = "2.5.13"
  }

  lazy val frontend = common ++ webjars ++ tests
  lazy val backend = common ++ metrics ++ tsimulus ++ tests

  val common = Seq(
    "com.typesafe.akka" %% "akka-actor" % Version.akka,
    "com.typesafe.akka" %% "akka-cluster" % Version.akka,
    "com.typesafe.akka" %% "akka-cluster-metrics" % Version.akka,
    "com.typesafe.akka" %% "akka-slf4j" % Version.akka,
	"com.typesafe.akka" %% "akka-remote" % Version.akka,
    "com.typesafe.akka" %% "akka-stream" %  "2.5.13",
    "com.google.guava" % "guava" % "18.0"
  )

  val webjars = Seq(
    "org.webjars" % "requirejs" % "2.1.11-1",
    "org.webjars" % "underscorejs" % "1.6.0-3",
    "org.webjars" % "jquery" % "1.11.1",
    "org.webjars" % "d3js" % "3.4.9",
    "org.webjars" % "bootstrap" % "3.2.0" exclude ("org.webjars", "jquery"),
    "org.webjars" % "bootswatch-yeti" % "3.2.0" exclude ("org.webjars", "jquery"),
    "org.webjars" % "angularjs" % "1.2.16-2" exclude ("org.webjars", "jquery"),
	"org.webjars" % "swagger-ui" % "2.2.0"
  )

  val metrics = Seq(
    "io.kamon" % "sigar-loader" % "1.6.6-rev002"
  )
  
  val tsimulus = Seq(
      "com.github.scopt" %% "scopt" % "3.5.0",
      "com.github.nscala-time" % "nscala-time_2.11" % "2.12.0",
      "org.apache.commons" % "commons-math3" % "3.6.1",
      "io.spray" %%  "spray-json" % "1.3.2",
      "org.scalactic" %% "scalactic" % "3.0.0",
      "be.cetic" %% "tsimulus" % "0.1.14",
	  "org.apache.kafka" %% "kafka" % "0.8.2.2",
      "org.apache.kafka" % "kafka-clients" % "0.8.2.2",
	  "org.apache.logging.log4j" % "log4j-core" % "2.7"
  )
	  
  val tests = Seq(
    "org.scalatest" %% "scalatest" % "2.2.4" % "test",
    "org.scalatestplus" %% "play" % "1.4.0-M3" % "test",
    "com.typesafe.akka" %% "akka-testkit" % Version.akka % "test"
  )

}
