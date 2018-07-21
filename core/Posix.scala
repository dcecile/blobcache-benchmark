package blobstoreBenchmark.core

import com.sun.jna._

trait Posix extends Library {
  def sysconf(name: Int): Long
  def times(buf: Array[Long]): Long
}

object Posix {
  val _SC_CLK_TCK: Int = 2

  @SuppressWarnings(
    Array("org.wartremover.warts.AsInstanceOf"))
  val instance: Posix =
    Native
      .loadLibrary("c", classOf[Posix])
      .asInstanceOf[Posix]
}
