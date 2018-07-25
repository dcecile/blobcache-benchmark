package blobstoreBenchmark.core

final case class Bench(
  userClockTicks: Long,
  systemClockTicks: Long,
  nanoTime: Long)

object Bench {
  val clockTicksPerSecond: Long =
    Posix.instance.sysconf(Posix._SC_CLK_TCK)

  def snapshot(): Bench = {
    val buffer = new Array[Long](4)
    val _ = Posix.instance.times(buffer)
    val nanoTime = System.nanoTime()
    val utime = buffer(0)
    val stime = buffer(1)
    val cutime = buffer(2)
    val cstime = buffer(3)
    Bench(utime + cutime, stime + cstime, nanoTime)
  }

  def report(
    description: String,
    block: => Unit
  ): Unit = {
    val start = snapshot()
    val () = block
    val end = snapshot()
    val userSeconds =
      toSeconds(
        start.userClockTicks,
        end.userClockTicks,
        clockTicksPerSecond)
    val systemSeconds =
      toSeconds(
        start.systemClockTicks,
        end.systemClockTicks,
        clockTicksPerSecond)
    val totalSeconds =
      toSeconds(start.nanoTime, end.nanoTime, 1000000000)
    println(
      Seq(
        s"${description}",
        s"user ${formatSeconds(userSeconds)}",
        s"system ${formatSeconds(systemSeconds)}",
        s"total ${formatSeconds(totalSeconds)}"
      ).mkString(" / "))
  }

  def toSeconds(
    start: Long,
    end: Long,
    conversion: Long
  ): Double =
    (end - start).toDouble / conversion.toDouble

  def formatSeconds(seconds: Double): String =
    f"${seconds}%1.2fs"
}
