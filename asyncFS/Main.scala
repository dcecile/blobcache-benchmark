package blobstoreBenchmark.asyncFS

import java.io.File
import java.nio.ByteBuffer
import java.nio.file.StandardOpenOption
import monix.eval.Task
import monix.execution.Scheduler
import monix.nio.file.TaskFileChannel
import monix.reactive.Observable
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.Function.tupled

import blobstoreBenchmark.core.Harness
import blobstoreBenchmark.core.Key
import blobstoreBenchmark.core.Pair
import blobstoreBenchmark.core.Plan
import blobstoreBenchmark.core.Step
import blobstoreBenchmark.core.Sum

object Main extends Harness {
  def init(plan: Plan): Unit =
    writeParallel(plan.dbDir, plan.blobSize, plan.pairs)

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
    writeParallel(plan.dbDir, plan.blobSize, step.updates)
    sum
  }

  def writeParallel(
    dbDir: File,
    blobSize: Int,
    pairs: Seq[Pair]
  ): Unit = {
    implicit val ctx: Scheduler = Scheduler.Implicits.global

    val future = Observable
      .fromIterable(pairs)
      .mapParallelUnordered(1)(pair =>
        Task(
          (pair.key, pair.blobStub.generateBuffer(blobSize))))
      .bufferIntrospective(200)
      .mapTask(pairs => {
        val batch = pairs.map(tupled(writeTask(dbDir, _, _)))
        Task.gatherUnordered(batch).map(_ => ())
      })
      .completedL
      .runAsync

    Await.result(future, 30.seconds)
  }

  @SuppressWarnings(
    Array("org.wartremover.warts.ImplicitParameter"))
  def writeTask(
    dbDir: File,
    key: Key,
    buffer: ByteBuffer
  )(
    implicit ctx: Scheduler
  ): Task[Unit] = {
    val file = new File(dbDir, key.toBase64)
    val channel = TaskFileChannel(
      file,
      StandardOpenOption.CREATE,
      StandardOpenOption.WRITE)
    channel
      .write(buffer, 0)
      .flatMap(_ => channel.close())
  }

  def read(
    dbDir: File,
    blobSize: Int,
    key: Key
  ): Long = {
    implicit val ctx: Scheduler = Scheduler.Implicits.global
    val file = new File(dbDir, key.toBase64)
    val buffer = ByteBuffer.allocateDirect(blobSize)
    val channel =
      TaskFileChannel(file, StandardOpenOption.READ)
    val future = channel
      .read(buffer, 0)
      .flatMap(_ => channel.close())
      .runAsync
    Await.result(future, 1.seconds)
    Sum.fromBuffer(buffer.rewind)
  }
}
