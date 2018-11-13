name := "tsaas-backend"
libraryDependencies ++= Dependencies.backend
version:= "1.0.0"

mainClass in(Compile, run) := Some("be.cetic.tsaas.Backend")
mainClass in(Compile, packageBin) := Some("be.cetic.backend.Backend")
