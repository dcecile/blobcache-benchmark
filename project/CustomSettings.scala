package blobstoreBenchmark.sbt

import sbt.Keys._
import sbt._
import wartremover.WartRemover.autoImport._

import Dependencies._
import FmtTask._

object CustomSettings {
  lazy val customScalacOptions =
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-feature",
      "-target:jvm-1.8",
      "-unchecked",
      "-Xfatal-warnings",
      "-Xfuture",
      "-Xlint",
      "-Xsource:2.12",
      "-Ywarn-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-extra-implicit",
      "-Ywarn-inaccessible",
      "-Ywarn-nullary-override",
      "-Ywarn-nullary-unit",
      "-Ywarn-numeric-widen",
      "-Ywarn-unused:_",
      "-Ywarn-unused-import",
      "-Ywarn-value-discard"
    )

  lazy val customSourceRules = Seq(
    scalaSource in Compile := baseDirectory.value
  )

  lazy val customWartremoverErrors = Seq(
    wartremoverErrors := Warts
      .allBut(Wart.Equals, Wart.Nothing)
  )

  lazy val allCustomSettings = (
    customScalaVersion
      ++ fmtTaskSettings
      ++ fmtCheckTaskSettings
      ++ customSourceRules
      ++ customScalacOptions
      ++ customResolvers
      ++ customWartremoverErrors
  )
}
