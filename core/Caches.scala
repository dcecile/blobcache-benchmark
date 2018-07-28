package blobstoreBenchmark.core

import scala.sys.process.Process

object Caches {
  @SuppressWarnings(
    Array(
      "org.wartremover.warts.Throw",
      "org.wartremover.warts.TraversableOps"))
  def drop(): Unit = {
    val command = Seq(
      "sudo",
      "sh",
      "-c",
      "sync; echo 3 >/proc/sys/vm/drop_caches")
    println(s"sudo: ${command.last}")
    val builder = Process(command)
    val process = builder.run(connectInput = true)
    if (process.exitValue() != 0) {
      throw new Exception("Drop caches failed")
    }
  }
}
