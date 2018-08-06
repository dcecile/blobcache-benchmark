package blobstoreBenchmark.fs2

import java.nio.file.Path
import cats.effect.IO
import fs2.Chunk
import fs2.Stream
import fs2.io.file.readAllAsync
import fs2.io.file.writeAllAsync
import scala.concurrent.ExecutionContext.Implicits.global

import blobstoreBenchmark.core.DiscardNonUnitValue.discard
import blobstoreBenchmark.core.Harness
import blobstoreBenchmark.core.Key
import blobstoreBenchmark.core.Pair
import blobstoreBenchmark.core.Plan
import blobstoreBenchmark.core.Step
import blobstoreBenchmark.core.Sum

object Main extends Harness {
  def init(plan: Plan): Unit = {
    val writes =
      writeAll(plan.dbDir.toPath, plan.blobSize, plan.pairs)
    writes.compile.drain.unsafeRunSync
  }

  def run(plan: Plan): Long =
    Stream
      .emits(plan.steps)
      .flatMap(runStep(plan, _))
      .compile
      .fold(0L)(_ + _)
      .unsafeRunSync

  def runStep(
    plan: Plan,
    step: Step
  ): Stream[IO, Long] = {
    val sum = Stream
      .emits(step.queries)
      .flatMap(read(plan.dbDir.toPath, _))
    val writes =
      writeAll(plan.dbDir.toPath, plan.blobSize, step.updates)
    sum ++ writes
  }

  def writeAll(
    dbDir: Path,
    blobSize: Int,
    pairs: Seq[Pair]
  ): Stream[IO, Nothing] =
    Stream
      .emits(pairs)
      .flatMap(writeTask(dbDir, blobSize, _))
      .drain

  def writeTask(
    dbDir: Path,
    blobSize: Int,
    pair: Pair
  ): Stream[IO, Byte] = {
    val buffer = pair.blobStub.generateDirectBuffer(blobSize)
    discard(buffer.flip)
    val path = dbDir.resolve(pair.key.toBase64)
    Stream
      .chunk(Chunk.byteBuffer(buffer))
      .observe(writeAllAsync[IO](path))
  }

  def read(
    dbDir: Path,
    key: Key
  ): Stream[IO, Long] = {
    val path = dbDir.resolve(key.toBase64)
    readAllAsync[IO](path, 4096).chunks.head
      .map(chunk => Sum.fromBuffer(chunk.toByteBuffer))
  }
}
