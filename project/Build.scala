import sbt._
import Keys._

//import com.twitter.sbt._
//import PackageDist._

object SzxcvbnBuild extends Build {

  val scalaTest  = "org.scalatest" %% "scalatest" % "1.8" % "test"

  val ufversion = "0.6.3"
  val uf = Seq(
    "net.databinder" %% "unfiltered-filter" % ufversion,
    "net.databinder" %% "unfiltered-json" % ufversion,
    "net.databinder" %% "unfiltered-jetty" % ufversion
  )

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := "eu.tekul",
    scalaVersion := "2.9.2",
    version      := "0.1",
    crossScalaVersions := Seq("2.8.2", "2.9.2")
  )

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

//  val packageDistSettings = GitProject.gitSettings ++ PackageDist.newSettings ++ Seq(
//    packageDistZipName := "szxcvbn-server.zip",
//    packageDistCopy ~= {
//      files => files.filter(f => !f.getName.contains("-javadoc") && !f.getName.contains("-sources") && !f.getName.startsWith("scala-compiler"))
//    }
//  )
  
  lazy val server = Project("server",
    file("server"), 
    settings = buildSettings ++  Seq(
      mainClass in Compile := Some("SzxcvbnServer"),
      publishArtifact := false,
      libraryDependencies ++= uf
    ) //++ packageDistSettings
  ) dependsOn(core)
}
