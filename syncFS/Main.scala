package blobstoreBenchmark.syncFS

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

import blobstoreBenchmark.core.Blob
import blobstoreBenchmark.core.Harness
import blobstoreBenchmark.core.Key
import blobstoreBenchmark.core.Plan
import blobstoreBenchmark.core.Step
import blobstoreBenchmark.core.Verify

object Main extends Harness {
  def init(plan: Plan): Unit =
    plan.keys.foreach(write(plan.dbDir, plan.blobSize, _))

  def run(plan: Plan): Unit = {
    val sum = plan.steps.toStream
      .map(runStep(plan, _))
      .sum
    Verify.sum("total", sum, plan.expectedSum)
  }

  def runStep(
    plan: Plan,
    step: Step
  ): Long = {
    val sum = step.queries.toStream
      .map(read(plan.dbDir, plan.blobSize, _))
      .sum
    step.updates
      .foreach(write(plan.dbDir, plan.blobSize, _))
    step.queries
      .foreach(delete(plan.dbDir, _))
    sum
  }

  def write(
    dbDir: File,
    blobSize: Int,
    key: Key
  ): Unit = {
    val blob = Blob.generate(key, blobSize)
    val file = new File(dbDir, key.toBase64)
    val fileOutputStream = new FileOutputStream(file)
    fileOutputStream.write(blob.array)
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
    val buffer = new Array[Byte](blobSize)
    if (fileInputStream.read(buffer) != blobSize) {
      throw new IOException(
        s"Problem reading ${file.getAbsolutePath()}");
    }
    fileInputStream.close()
    Verify.blobSum(ByteBuffer.wrap(buffer), key, blobSize)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def delete(dbDir: File, key: Key): Unit = {
    val file = new File(dbDir, key.toBase64)
    if (!file.delete()) {
      throw new IOException(
        s"Cannot delete ${file.getAbsolutePath()}");
    }
  }
}
