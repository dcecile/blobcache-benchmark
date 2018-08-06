package blobstoreBenchmark.core

import java.io.File
import org.apache.commons.io.FileUtils
import org.rogach.scallop._

trait Harness {
  def init(plan: Plan): Unit

  def run(plan: Plan): Long

  def main(args: Array[String]): Unit = {
    val conf = new Harness.Conf(args)
    conf.subcommand match {
      case Some(conf.clean) =>
        harnessClean()
      case Some(conf.init) =>
        harnessInit(conf.init.keyCount())
      case Some(conf.run) =>
        harnessRun(conf.run.keyCount(), conf.run.stepCount())
      case _ =>
        conf.printHelp()
    }
  }

  private def harnessClean(): Unit =
    Bench.report(dbDir, describeTask("clean"), deleteDbDir())

  private def harnessInit(
    keyCount: Int
  ): Unit = {
    harnessClean()
    makeDbDir()
    val plan = generatePlan(keyCount, stepCount = 0)
    Bench.report(
      dbDir,
      describePlanTask("init", plan),
      init(plan))
  }

  private def harnessRun(
    keyCount: Int,
    stepCount: Int
  ): Unit = {
    Bench.report(
      dbDir,
      describeTask("dropCaches"),
      Caches.drop())
    val plan = generatePlan(keyCount, stepCount)
    val sum =
      Bench.reportAndLog(
        dbDir,
        describePlanTask("run", plan),
        name,
        plan.keyCount,
        run(plan))
    Verify.sum("total", sum, plan.expectedSum)
  }

  private def generatePlan(
    keyCount: Int,
    stepCount: Int
  ): Plan =
    Bench.report(
      dbDir,
      describeTask("plan"),
      Plan.generate(dbDir, keyCount, stepCount))

  private lazy val name =
    this.getClass().getName().split('.')(1)

  private val dbDir: File =
    new File("db")

  private def makeDbDir(): Unit =
    FileUtils.forceMkdir(dbDir)

  private def deleteDbDir(): Unit =
    if (dbDir.exists()) {
      FileUtils.deleteDirectory(dbDir)
    }

  private def describeTask(task: String): String =
    s"${name} ${task}"

  private def describePlanTask(
    task: String,
    plan: Plan
  ): String =
    s"${describeTask(task)} ${plan.keyCount} ${plan.stepCount} ${plan.blobSize}"
}

object Harness {
  class Conf(arguments: Seq[String])
      extends ScallopConf(arguments) {
    object clean extends Subcommand("clean")
    addSubcommand(clean)

    object init extends Subcommand("init") {
      val keyCount: ScallopOption[Int] = trailArg[Int]()
      val ignored: ScallopOption[Int] =
        trailArg[Int](required = false)
    }
    addSubcommand(init)

    object run extends Subcommand("run") {
      val keyCount: ScallopOption[Int] = trailArg[Int]()
      val stepCount: ScallopOption[Int] = trailArg[Int]()
    }
    addSubcommand(run)

    verify()
  }
}
