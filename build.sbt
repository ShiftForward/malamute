organization  := "org.shiftforward"

version       := "0.1"

scalaVersion  := "2.11.7"

scalacOptions := Seq("-unchecked", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.3.11"
  val sprayV = "1.3.3"
  val specsV = "2.4.17"
  Seq(
    "io.spray"            %%  "spray-can"         % sprayV,
    "io.spray"            %%  "spray-routing"     % sprayV,
    "io.spray"            %%  "spray-json"        % "1.3.2",
    "io.spray"            %%  "spray-testkit"     % sprayV   % "test",
    "org.specs2"          %%  "specs2-core"       % specsV   % "test",
    "org.specs2"          %%  "specs2-junit"      % specsV   % "test",
    "org.specs2"          %%  "specs2-scalacheck" % specsV   % "test",
    "com.typesafe.akka"   %%  "akka-actor"        % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"      % akkaV    % "test",
    "org.json4s"          %%  "json4s-native"     % "3.2.11",
    "com.gettyimages"     %%  "spray-swagger"     % "0.5.1",
    "ch.qos.logback"      %   "logback-classic"   % "1.1.3",
    "com.sksamuel.elastic4s" %% "elastic4s" % "1.2.1.2"
  )
}

scalariformSettings
