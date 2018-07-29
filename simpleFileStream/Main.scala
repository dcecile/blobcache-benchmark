package blobstoreBenchmark.simpleFileStream

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

import blobstoreBenchmark.core.Harness
import blobstoreBenchmark.core.Key
import blobstoreBenchmark.core.Pair
import blobstoreBenchmark.core.Plan
import blobstoreBenchmark.core.Step
import blobstoreBenchmark.core.Sum

object Main extends Harness {
  def init(plan: Plan): Unit =
    plan.pairs.foreach(write(plan.dbDir, plan.blobSize, _))

  def run(plan: Plan): Long =
    plan.steps.toStream
      .map(runStep(plan, _))
      .sum

  def runStep(
    plan: Plan,
    step: Step
  ): Long = {
    val sum = step.queries.toStream
      .map(read(plan.dbDir, plan.blobSize, _))
      .sum
    step.updates
      .foreach(write(plan.dbDir, plan.blobSize, _))
    sum
  }

  def write(
    dbDir: File,
    blobSize: Int,
    pair: Pair
  ): Unit = {
    val file = new File(dbDir, pair.key.toBase64)
    val fileOutputStream = new FileOutputStream(file)
    fileOutputStream.write(
      pair.blobStub.generateArray(blobSize))
    fileOutputStream.close()
  }

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def read(
    dbDir: File,
    blobSize: Int,
    key: Key
  ): Long = {
    val file = new File(dbDir, key.toBase64)
    val fileInputStream = new FileInputStream(file)
    val array = new Array[Byte](blobSize)
    if (fileInputStream.read(array) != blobSize) {
      throw new IOException(
        s"Problem reading ${file.getAbsolutePath()}");
    }
    fileInputStream.close()
    Sum.fromArray(array)
  }
}
