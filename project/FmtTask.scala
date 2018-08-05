package blobstoreBenchmark.sbt

import org.scalafmt.sbt.ScalafmtPlugin.autoImport._
import sbt._

object FmtTask {
  lazy val fmt = taskKey[Unit]("format all code")

  lazy val fmtCheck =
    taskKey[Unit]("check all code formatting")

  lazy val fmtTaskSettings = Seq(fmt := {
    (Compile / scalafmt).value
    (Compile / scalafmtSbt).value
  })

  lazy val fmtCheckTaskSettings = Seq(fmtCheck := {
    (Compile / scalafmtCheck).value
    (Compile / scalafmtSbtCheck).value
  })
}
