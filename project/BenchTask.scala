package blobstoreBenchmark.sbt

import sbt.Keys._
import sbt._

object BenchTask {
  lazy val benchOne =
    inputKey[Unit]("run benchmarks for one project")

  lazy val bench =
    inputKey[Unit]("run benchmarks for many projects")

  lazy val benchOneTaskSettings = Seq(
    Compile / benchOne := (Def.inputTaskDyn {
      Def.sequential(
        (Compile / run).toTask(" init 100"),
        (Compile / run).toTask(" run 100 1000"),
        (Compile / run).toTask(" init 1000"),
        (Compile / run).toTask(" run 1000 1000"),
        (Compile / run).toTask(" clean")
      )
    }).evaluated)

  def benchTaskSettings(projects: Seq[ProjectReference]) =
    Seq(
      bench := (Def.inputTaskDyn {
        val tasks = projects.map(project =>
          (project / Compile / benchOne).toTask(""))
        Def.sequential(tasks, Def.task { () })
      }).evaluated
    )
}
