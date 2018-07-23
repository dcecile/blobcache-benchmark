package blobstoreBenchmark.syncFS

import com.github.dwickern.macros.NameOf._
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException;

import blobstoreBenchmark.core.Blob
import blobstoreBenchmark.core.Bench
import blobstoreBenchmark.core.Harness
import blobstoreBenchmark.core.Key
import blobstoreBenchmark.core.Plan
import blobstoreBenchmark.core.Step
import blobstoreBenchmark.core.Verify

object Main extends App {
  private def run(): Unit = {
    val plan = Plan.generate()
    val dbDir = Harness.makeEmptyDbDir()

    report(
      plan, {
        plan.keys.foreach(write(dbDir, plan.blobSize, _))
      }
    )

    report(
      plan, {
        val sum = plan.steps.toStream
          .map(runStep(dbDir, plan, _))
          .sum
        Verify.sum("total", sum, plan.expectedSum)
      }
    )
  }

  private def runStep(
    dbDir: File,
    plan: Plan,
    step: Step
  ): Long = {
    val sum = step.queries.toStream
      .map(read(dbDir, plan.blobSize, _))
      .sum
    step.updates
      .foreach(write(dbDir, plan.blobSize, _))
    step.queries
      .foreach(delete(dbDir, _))
    sum
  }

  private def write(
    dbDir: File,
    blobSize: Int,
    key: Key
  ): Unit = {
    val blob = Blob.generate(key, blobSize)
    val file = new File(dbDir, key.toBase64)
    val fileOutputStream = new FileOutputStream(file)
    fileOutputStream.write(blob)
    fileOutputStream.close()
  }

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  private def read(
    dbDir: File,
    blobSize: Int,
    key: Key): Long = {
    val file = new File(dbDir, key.toBase64)
    val fileInputStream = new FileInputStream(file)
    val blob = new Array[Byte](blobSize)
    if (fileInputStream.read(blob) != blobSize) {
      throw new IOException(
        s"Problem reading ${file.getAbsolutePath()}");
    }
    val sum = Verify.blobSum(blob, key, blobSize)
    fileInputStream.close()
    sum
  }

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def delete(dbDir: File, key: Key): Unit = {
    val file = new File(dbDir, key.toBase64)
    if (!file.delete()) {
      throw new IOException(
        s"Cannot delete ${file.getAbsolutePath()}");
    }
  }

  private def report =
    Bench.report(qualifiedNameOfType[this.type])(_, _)

  run()
}
