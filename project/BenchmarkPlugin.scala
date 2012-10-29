import sbt._
import Keys._

object BenchmarkPlugin {

  def benchmarkSettings = Seq(
    libraryDependencies ++= Seq(
      "com.google.code.caliper" % "caliper" % "1.0-SNAPSHOT" from "http://n0d.es/jars/caliper-1.0-SNAPSHOT.jar",
      "com.google.code.java-allocation-instrumenter" % "java-allocation-instrumenter" % "2.0",
      "com.google.code.gson" % "gson" % "1.7.1"
    ),

    fork in run := true,

    javaOptions in run <<= (fullClasspath in Runtime) map { cp => Seq("-cp", Build.data(cp).mkString(":")) }
  )
}
