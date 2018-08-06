package blobstoreBenchmark.core

import scala.sys.process.Process

object Caches {
  def drop(): Unit =
    sudo("sync; echo 3 >/proc/sys/vm/drop_caches")

  def syncAfter[T](block: => T): T = {
    val value: T = block
    sudo("sync")
    value
  }

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  private def sudo(sudoCommand: String): Unit = {
    val fullCommand = Seq("sudo", "sh", "-c", sudoCommand)
    println(s"sudo: ${sudoCommand}")
    val builder = Process(fullCommand)
    val process = builder.run(connectInput = true)
    if (process.exitValue() != 0) {
      throw new Exception("Drop caches failed")
    }
  }
}
