package blobstoreBenchmark.core

import java.io.File
import org.apache.commons.io.FileUtils

import DiscardNonUnitValue.discard

final case class Bench(
  userClockTicks: Long,
  systemClockTicks: Long,
  nanoTime: Long,
  totalSize: Long)

object Bench {
  def report[T](
    dbDir: File,
    description: String,
    block: => T
  ): T = {
    val (value, result) = measure(dbDir, "", 0, block)
    printResult(description, result)
    value
  }

  def reportAndLog[T](
    dbDir: File,
    description: String,
    blobstore: String,
    keyCount: Int,
    block: => T
  ): T = {
    val (value, result) =
      measure(dbDir, blobstore, keyCount, block)
    printResult(description, result)
    logResult(result)
    value
  }

  def measure[T](
    dbDir: File,
    blobstore: String,
    keyCount: Int,
    block: => T
  ): (T, Result) = {
    val start = snapshotStart()
    val value: T = block
    val end = snapshotEnd(dbDir)
    (value, calculateResult(blobstore, keyCount, start, end))
  }

  def snapshotStart(): Bench =
    snapshotTime()(0)

  def snapshotEnd(dbDir: File): Bench = {
    val time = snapshotTime()
    val size = snapshotSize(dbDir)
    time(size)
  }

  def snapshotSize(dbDir: File): Long =
    if (dbDir.exists()) {
      FileUtils.sizeOfDirectory(dbDir)
    } else {
      0
    }

  def snapshotTime(): Long => Bench = {
    val buffer = new Array[Long](4)
    discard(Posix.instance.times(buffer))
    val nanoTime = System.nanoTime()
    val utime = buffer(0)
    val stime = buffer(1)
    val cutime = buffer(2)
    val cstime = buffer(3)
    Bench(utime + cutime, stime + cstime, nanoTime, _)
  }

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
      toSeconds(start.nanoTime, end.nanoTime, 1000000000),
      end.totalSize
    )

  def toSeconds(
    start: Long,
    end: Long,
    conversion: Long
  ): Double =
    (end - start).toDouble / conversion.toDouble

  val clockTicksPerSecond: Long =
    Posix.instance.sysconf(Posix._SC_CLK_TCK)

  def printResult(description: String, result: Result): Unit =
    println(
      Seq(
        s"${description}",
        s"user ${formatSeconds(result.userSeconds)}",
        s"system ${formatSeconds(result.systemSeconds)}",
        s"total ${formatSeconds(result.totalSeconds)}",
        s"size ${formatBytes(result.totalSizeBytes)}"
      ).mkString(" / "))

  def formatSeconds(seconds: Double): String =
    f"${seconds}%1.2fs"

  def formatBytes(bytes: Long): String =
    FileUtils.byteCountToDisplaySize(bytes)

  def logResult(result: Result): Unit =
    Result.saveAll(result +: Result.loadAll())
}
