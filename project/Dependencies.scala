package tacit.sbt

import sbt.Def._
import sbt.Keys._
import sbt._

object Dependencies {
  lazy val customScalaVersion = scalaVersion := "2.12.4"

  lazy val jna =
    setting(
      "net.java.dev.jna" % "jna" % "4.5.2")

  lazy val nameof =
    setting(
      "com.github.dwickern" %% "scala-nameof" % "1.0.3" % Provided)

  lazy val customResolvers =
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases")
    )

  def toSettings(
    dependencies: Seq[Def.Initialize[ModuleID]]
  ) =
    dependencies.map(dependency =>
      libraryDependencies += dependency.value)
}
