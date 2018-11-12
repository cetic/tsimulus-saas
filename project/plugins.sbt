// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Git Plugin https://blog.softwaremill.com/meaningful-docker-image-tags-made-with-build-tools-c8877cd21da9
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.9.3")

// Docker Plugin
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.4")

// Use the Play sbt plugin for Play projects
/*addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.7")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.4.0")

// addSbtPlugin("com.iheart" % "sbt-play-swagger" % "0.6.5")

addSbtPlugin("com.iheart" % "sbt-play-swagger" % "0.6.0-PLAY2.4")

// Node.js plugin https://github.com/sbt/sbt-js-engine
//addSbtPlugin("com.typesafe.sbt" % "sbt-js-engine" % "1.2.1")

*/
