package blobstoreBenchmark.syncFS

import com.github.dwickern.macros.NameOf._
import java.io.File
import java.io.FileOutputStream

import blobstoreBenchmark.core.Blob
import blobstoreBenchmark.core.Bench
import blobstoreBenchmark.core.Harness
import blobstoreBenchmark.core.Key

object Main extends App {
  private def run(): Unit = {
    Key.seed()

    val count = 10000
    val size = 4096
    val keys = Key.createInitialKeys(count)
    val dbDir = Harness.makeEmptyDbDir()

    report(
      count, {
        for (key <- keys) {
          val blob = Blob.fromKey(key, size)
          val file = new File(dbDir, Key.toBase64(key))
          val fileOutputStream = new FileOutputStream(file)
          fileOutputStream.write(blob)
          fileOutputStream.close()
        }
      }
    )
  }

  private def report =
    Bench.report(qualifiedNameOfType[this.type])(_, _)

  run()
}
