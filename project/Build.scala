import sbt._
import Keys._


object SzxcvbnBuild extends Build {
  val scalaTest  = "org.scalatest" %% "scalatest" % "1.9.1" % "test"

  val ufversion = "0.6.5"
  val uf = Seq(
    "net.databinder" %% "unfiltered-filter" % ufversion,
    "net.databinder" %% "unfiltered-netty-server" % ufversion
  )

  val spray = "io.spray" %%  "spray-json" % "1.2.3"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := "eu.tekul",
    scalaVersion := "2.10.0",
    version      := "0.3-SNAPSHOT",
    crossScalaVersions := Seq("2.8.2", "2.9.2")
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings

  lazy val szxcvbn = Project("root",
    file("."),
    settings = buildSettings ++ Seq(publishArtifact := false)
  ) aggregate (core, server)

  lazy val core = Project("szxcvbn",
    file("core"), settings = buildSettings  ++ Seq(
      libraryDependencies += scalaTest,
      compileOrder := CompileOrder.ScalaThenJava,
      exportJars := true
    ) ++ Publish.settings
  )

  import VmcPlugin._

  val cfAppSettings = vmcSettings ++ Seq(
    // Filter out the scalap deps from lift-json
    vmcPackageCopy ~= {files => files.filter(!_.getName.matches("scala(-compiler|p).*"))}
  )

  lazy val server = Project("server",
    file("server"),
    settings = buildSettings ++ Seq(
      mainClass in Compile := Some("szxcvbn.Server"),
      publishArtifact := false,
      resolvers += "io.spray" at "http://repo.spray.io/",
      libraryDependencies ++= uf ++ Seq(spray)
    ) ++ cfAppSettings
  ) dependsOn core

  import BenchmarkPlugin._

  lazy val benchmark = Project("benchmark",
    file("benchmark"), settings = buildSettings ++ benchmarkSettings ++ Seq(publishArtifact := false)) dependsOn core


}
