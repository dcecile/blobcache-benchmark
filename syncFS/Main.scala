package blobstoreBenchmark.syncFS

import com.github.dwickern.macros.NameOf._

import blobstoreBenchmark.core.Keys

object Main extends App {
  def run(): Unit = {
    println(qualifiedNameOfType[this.type])
    Keys.seed()
    val keys = Keys.createInitialKeys(2000000)
    println(s"${keys.length}")
  }

  run()
}
