import bintray.Plugin._

seq(bintraySettings:_*)

name := "akka-elasticsearch-logger"

organization := "com.github.dnvriend"

version := "1.0.0"

scalaVersion := "2.11.2"

resolvers += "dnvriend at bintray" at "http://dl.bintray.com/dnvriend/maven"

libraryDependencies ++=
  {
    val akkaV = "2.3.6"
    val jsonV = "1.2.6"
    val esV = "1.3.2"
    Seq(
      "org.scala-lang"          % "scala-library"                  % scalaVersion.value,
      "com.typesafe.akka"      %% "akka-actor"                     % akkaV,
      "io.spray"               %% "spray-json"                     % jsonV,
      "com.sksamuel.elastic4s" %% "elastic4s"                      % esV,
      "ch.qos.logback"          % "logback-classic"                % "1.1.2",
      "com.github.dnvriend"    %% "akka-elasticsearch"             % "1.0.0",
      "org.scalatest"          %% "scalatest"                      % "2.1.4" % "test"
    )
  }

autoCompilerPlugins := true

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

publishMavenStyle := true

licenses += ("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php"))

bintray.Keys.packageLabels in bintray.Keys.bintray := Seq("akka", "elasticsearch", "logback", "logging", "index", "indexing")