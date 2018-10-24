val commonSettings = Seq(
  organization := "cetic",
  version := "0.0.1",
  scalaVersion := "2.11.7",
  // git info
  git.formattedShaVersion := git.gitHeadCommit.value map { sha =>
			s"$sha".substring(0, 8)
  },
  // docker info
  dockerRepository :=  Some("nexus.ext.cetic.be:8083"),
  dockerBaseImage := "openjdk:8",
  dockerUpdateLatest := true,
  dockerAlias := DockerAlias(dockerRepository.value, dockerUsername.value, name.value, git.formattedShaVersion.value),
  dockerUsername := Some("cetic")
)
/*
  // build info
  buildInfoPackage := "meta",
  buildInfoOptions += BuildInfoOption.ToJson,
  buildInfoKeys := Seq[BuildInfoKey](
    name, version, scalaVersion,
    "sbtNativePackager" -> "1.0.0"
  )
)

*/
lazy val backend = (project in file("backend"))
    .enablePlugins(DockerPlugin, GitVersioning)
    .settings(
        name := "tsaas-backend",
        libraryDependencies ++= (Dependencies.tsimulus ++ Dependencies.common),
        commonSettings
    )

lazy val root = (project in file("."))
  .settings(
    name := "tsimulus-saas"
  )
  
//
// Scala Compiler Options
// If this project is only a subproject, add these to a common project setting.
//
scalacOptions in ThisBuild ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-deprecation", // warning and location for usages of deprecated APIs
  "-feature", // warning and location for usages of features that should be imported explicitly
  "-unchecked", // additional warnings where generated code depends on assumptions
  "-Xlint", // recommended additional warnings
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
  "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
  "-Ywarn-inaccessible",
  "-Ywarn-dead-code"
)
