package blobstoreBenchmark.sbt

import sbt.Keys._
import sbt._
import sbt.complete.DefaultParsers._
import sbt.complete.Parser._
import sbt.complete._
import scala.math

object BenchTask {
  lazy val benchOne =
    inputKey[Unit]("run benchmarks for one project")

  lazy val bench =
    inputKey[Unit]("run benchmarks for many projects")

  lazy val benchParser: Parser[(Int, Int)] =
    (DefaultParsers.literal(" ") ~> NatBasic) ~ (DefaultParsers
      .literal(" ") ~> NatBasic)

  lazy val benchOneTaskSettings = Seq(
    Compile / benchOne := (Def.inputTaskDyn {
      val (max, scale) = benchParser.parsed
      executeBenchmark(max, scale)
    }).evaluated)

  def executeBenchmark(max: Int, scale: Int) =
    (Def.taskDyn {
      val args = planBenchmarkArgs(max, scale)
      val tasks = args.map((Compile / run).toTask(_))
      Def.sequential(
        tasks,
        Def.task { () }
      )
    })

  def planBenchmarkArgs(max: Int, scale: Int): Seq[String] =
    planBenchmarkKeyCounts(max, scale)
      .flatMap(keyCount =>
        Seq(s" init ${keyCount}", s" run ${keyCount} 100")) :+ " clean"

  def planBenchmarkKeyCounts(max: Int, scale: Int): Seq[Int] =
    Stream
      .from(0)
      .map(i =>
        100 * Math.pow(10, i.toDouble / scale.toDouble).toInt)
      .takeWhile(_ <= max)

  def benchTaskSettings(projects: Seq[ProjectReference]) =
    Seq(
      bench := (Def.inputTaskDyn {
        val (max, scale) = benchParser.parsed
        delegateBenchOne(projects, max, scale)
      }).evaluated
    )

  def delegateBenchOne(
    projects: Seq[ProjectReference],
    max: Int,
    scale: Int
  ) =
    (Def.taskDyn {
      val tasks = projects.map(project =>
        Def.taskDyn {
          (project / Compile / benchOne)
            .toTask(s" ${max} ${scale}")
      })
      Def.sequential(tasks, Def.task { () })
    })
}
