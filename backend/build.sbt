name := "tsaas-backend"
libraryDependencies ++= Dependencies.backend

version:= "0.1"

mainClass in(Compile, run) := Some("be.cetic.tsaas.Backend")
mainClass in(Compile, packageBin) := Some("be.cetic.backend.Backend")
