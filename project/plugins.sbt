// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Git Plugin https://blog.softwaremill.com/meaningful-docker-image-tags-made-with-build-tools-c8877cd21da9
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.9.3")

// Docker Plugin
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.4")