package blobstoreBenchmark.syncFS

import com.github.dwickern.macros.NameOf._

import blobstoreBenchmark.core.Bench
import blobstoreBenchmark.core.Keys

object Main extends App {
  private def run(): Unit = {
    Keys.seed()
    val count = 20000000
    report(count, {
      val keys = Keys.createInitialKeys(count)
      Thread.sleep(1000)
      println(s"${keys.length}")
    })
  }

  private def report =
    Bench.report(qualifiedNameOfType[this.type])(_, _)

  run()
}
