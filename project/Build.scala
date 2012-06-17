import sbt._
import Keys._

import com.twitter.sbt._
import PackageDist._

object SzxcvbnBuild extends Build {

  val scalaTest  = "org.scalatest" %% "scalatest" % "1.8" % "test"

  val ufversion = "0.6.3"
  val uf = Seq(
    "net.databinder" %% "unfiltered-filter" % ufversion,
    "net.databinder" %% "unfiltered-json" % ufversion,
    "net.databinder" %% "unfiltered-jetty" % ufversion
  )

  val buildSettings = Defaults.defaultSettings ++ Seq (
    scalaVersion := "2.9.1",
    organization := "szxcvbn",
    version      := "0.1-SNAPSHOT"
  )

  lazy val szxcvbn = Project("szxcvbn", 
    file("."), 
    settings = buildSettings
  ) aggregate (core, server)

  lazy val core = Project("core", 
    file("core"), settings = buildSettings  ++ Seq(
      libraryDependencies += scalaTest,
      exportJars := true
    )
  )
  
  lazy val server = Project("szxcvbn-server",
    file("server"), 
    settings = buildSettings ++ GitProject.gitSettings ++ PackageDist.newSettings ++ Seq(
      mainClass in Compile := Some("SzxcvbnServer"),
      packageDistZipName := "szxcvbn-server.zip",
      libraryDependencies ++= uf,
      packageDistCopy ~= {
        files => files.filter(f => !f.getName.contains("-javadoc") && !f.getName.contains("-sources") && !f.getName.startsWith("scala-compiler"))
      }
    )
  ) dependsOn(core)
}
