package blobstoreBenchmark.core

import DiscardNonUnitValue.discard

final case class Bench(
  userClockTicks: Long,
  systemClockTicks: Long,
  nanoTime: Long)

object Bench {
  def report[T](
    description: String,
    block: => T
  ): T = {
    val (value, result) = measure("", 0, block)
    printResult(description, result)
    value
  }

  def reportAndLog[T](
    description: String,
    blobstore: String,
    keyCount: Int,
    block: => T
  ): T = {
    val (value, result) = measure(blobstore, keyCount, block)
    printResult(description, result)
    logResult(result)
    value
  }

  def measure[T](
    blobstore: String,
    keyCount: Int,
    block: => T
  ): (T, Result) = {
    val start = snapshot()
    val value: T = block
    val end = snapshot()
    (value, calculateResult(blobstore, keyCount, start, end))
  }

  def snapshot(): Bench = {
    val buffer = new Array[Long](4)
    discard(Posix.instance.times(buffer))
    val nanoTime = System.nanoTime()
    val utime = buffer(0)
    val stime = buffer(1)
    val cutime = buffer(2)
    val cstime = buffer(3)
    Bench(utime + cutime, stime + cstime, nanoTime)
  }

  def toSeconds(
    start: Long,
    end: Long,
    conversion: Long
  ): Double =
    (end - start).toDouble / conversion.toDouble

  def calculateResult(
    blobstore: String,
    keyCount: Int,
    start: Bench,
    end: Bench
  ): Result =
    Result(
      blobstore,
      keyCount,
      toSeconds(
        start.userClockTicks,
        end.userClockTicks,
        clockTicksPerSecond),
      toSeconds(
        start.systemClockTicks,
        end.systemClockTicks,
        clockTicksPerSecond),
      toSeconds(start.nanoTime, end.nanoTime, 1000000000)
    )

  val clockTicksPerSecond: Long =
    Posix.instance.sysconf(Posix._SC_CLK_TCK)

  def printResult(description: String, result: Result): Unit =
    println(
      Seq(
        s"${description}",
        s"user ${formatSeconds(result.userSeconds)}",
        s"system ${formatSeconds(result.systemSeconds)}",
        s"total ${formatSeconds(result.totalSeconds)}"
      ).mkString(" / "))

  def formatSeconds(seconds: Double): String =
    f"${seconds}%1.2fs"

  def logResult(result: Result): Unit =
    Result.saveAll(result +: Result.loadAll())
}
