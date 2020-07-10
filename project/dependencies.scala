import sbt._

object Dependencies {

  object Version {
    val akka = "2.5.13"
  }

  val backend = Seq(
    "com.github.scopt" %% "scopt" % "3.5.0",
    "io.spray" %%  "spray-json" % "1.3.2",
    "org.scalactic" %% "scalactic" % "3.0.0",
    "be.cetic" %% "tsimulus" % "0.1.18",
    "com.typesafe.akka" %% "akka-actor" % Version.akka,
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.5",
    "com.typesafe.akka" %% "akka-http" % "10.1.5",
    "com.typesafe.akka" %% "akka-stream" %  Version.akka,
    //"be.cetic" %% "rts-gen" % "0.1.13",
    "org.scalatest" %% "scalatest" % "3.0.0" % "test",
    "org.freemarker" % "freemarker" % "2.3.28",
    "javax.ws.rs" % "javax.ws.rs-api" % "2.0.1",
    "com.github.swagger-akka-http" %% "swagger-akka-http" % "2.0.0",
    "com.github.swagger-akka-http" %% "swagger-scala-module" % "2.0.2",
    "ch.megard" %% "akka-http-cors" % "0.3.0"
  )

}
