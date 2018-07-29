package blobstoreBenchmark.simpleFiles

import java.nio.file.Files
import java.nio.file.Path

import blobstoreBenchmark.core.Harness
import blobstoreBenchmark.core.Key
import blobstoreBenchmark.core.Pair
import blobstoreBenchmark.core.Plan
import blobstoreBenchmark.core.Step
import blobstoreBenchmark.core.Sum

object Main extends Harness {
  def init(plan: Plan): Unit =
    plan.pairs.foreach(
      write(plan.dbDir.toPath, plan.blobSize, _))

  def run(plan: Plan): Long =
    plan.steps.toStream
      .map(runStep(plan, _))
      .sum

  def runStep(
    plan: Plan,
    step: Step
  ): Long = {
    val sum = step.queries.toStream
      .map(read(plan.dbDir.toPath, _))
      .sum
    step.updates
      .foreach(write(plan.dbDir.toPath, plan.blobSize, _))
    sum
  }

  def write(
    dbDir: Path,
    blobSize: Int,
    pair: Pair
  ): Unit = {
    val path = dbDir.resolve(pair.key.toBase64)
    val _ =
      Files.write(path, pair.blobStub.generateArray(blobSize))
  }

  def read(
    dbDir: Path,
    key: Key
  ): Long = {
    val path = dbDir.resolve(key.toBase64)
    val array = Files.readAllBytes(path)
    Sum.fromArray(array)
  }
}
