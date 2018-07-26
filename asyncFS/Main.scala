package blobstoreBenchmark.asyncFS

import java.io.File
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import monix.eval.Task
import monix.execution.Scheduler
import monix.nio.file.TaskFileChannel
import monix.reactive.Observable
import scala.concurrent.Await
import scala.concurrent.duration._

import blobstoreBenchmark.core.Blob
import blobstoreBenchmark.core.Harness
import blobstoreBenchmark.core.Key
import blobstoreBenchmark.core.Plan
import blobstoreBenchmark.core.Step
import blobstoreBenchmark.core.Verify

object Main extends Harness {
  def init(plan: Plan): Unit =
    writeParallel(plan.dbDir, plan.blobSize, plan.keys)

  def run(plan: Plan): Unit = {
    val sum = plan.steps.toStream
      .map(runStep(plan, _))
      .sum
    Verify.sum("total", sum, plan.expectedSum)
  }

  private def runStep(
    plan: Plan,
    step: Step
  ): Long = {
    val sum = step.queries.toStream
      .map(read(plan.dbDir, plan.blobSize, _))
      .sum
    writeParallel(plan.dbDir, plan.blobSize, step.updates)
    step.queries
      .foreach(delete(plan.dbDir, _))
    sum
  }

  private def writeParallel(
    dbDir: File,
    blobSize: Int,
    keys: Seq[Key]
  ): Unit = {
    implicit val ctx: Scheduler = Scheduler.Implicits.global

    val future = Observable
      .fromIterable(keys)
      .mapParallelUnordered(1)(key =>
        Task((key, Blob.generate(key, blobSize))))
      .bufferIntrospective(200)
      .mapTask(pairs => {
        val batch = pairs.map({
          case (key, blob) => writeTask(dbDir, key, blob)
        })
        Task.gatherUnordered(batch).map(_ => ())
      })
      .completedL
      .runAsync

    Await.result(future, 30.seconds)
  }

  @SuppressWarnings(
    Array("org.wartremover.warts.ImplicitParameter"))
  private def writeTask(
    dbDir: File,
    key: Key,
    blob: ByteBuffer
  )(
    implicit ctx: Scheduler
  ): Task[Unit] = {
    val file = new File(dbDir, key.toBase64)
    val channel = TaskFileChannel(
      file,
      StandardOpenOption.CREATE,
      StandardOpenOption.WRITE)
    channel
      .write(blob, 0)
      .flatMap(_ => channel.close())
  }

  private def read(
    dbDir: File,
    blobSize: Int,
    key: Key
  ): Long = {
    implicit val ctx: Scheduler = Scheduler.Implicits.global
    val file = new File(dbDir, key.toBase64)
    val blob = ByteBuffer.allocateDirect(blobSize)
    val channel =
      TaskFileChannel(file, StandardOpenOption.READ)
    val future = channel
      .read(blob, 0)
      .flatMap(_ => channel.close())
      .runAsync
    Await.result(future, 1.seconds)
    Verify.blobSum(blob.rewind, key, blobSize)
  }

  def delete(dbDir: File, key: Key): Unit = {
    val file = new File(dbDir, key.toBase64)
    Files.delete(file.toPath)
  }
}
