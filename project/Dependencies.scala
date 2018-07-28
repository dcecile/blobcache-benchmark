package tacit.sbt

import sbt.Def._
import sbt.Keys._
import sbt._

object Dependencies {
  lazy val customScalaVersion = scalaVersion := "2.12.4"

  lazy val jna =
    setting(
      "net.java.dev.jna" % "jna" % "4.5.2")

  lazy val commonsIo =
    setting(
      "commons-io" % "commons-io" % "2.6")

  lazy val scallop =
    setting(
      "org.rogach" %% "scallop" % "3.1.2")

  lazy val monix =
    setting(
      "io.monix" %% "monix" % "3.0.0-RC1")

  lazy val monixnio =
    setting(
      "io.monix" %% "monix-nio" % "0.0.3")

  lazy val h2database =
    setting(
      "com.h2database" % "h2" % "1.4.197")

  lazy val rocksdbjni =
    setting(
      "org.rocksdb" % "rocksdbjni" % "5.14.2")

  lazy val lmdbjava =
    setting(
      "org.lmdbjava" % "lmdbjava" % "0.6.1")

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
