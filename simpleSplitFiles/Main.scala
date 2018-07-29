package blobstoreBenchmark.simpleSplitFiles

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
      writeNew(plan.dbDir.toPath, plan.blobSize, _))

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

  @SuppressWarnings(
    Array("org.wartremover.warts.NonUnitStatements"))
  def writeNew(
    dbDir: Path,
    blobSize: Int,
    pair: Pair
  ): Unit = {
    val (dir, _) = resolve(dbDir, pair.key)
    Files.createDirectories(dir)
    write(dbDir, blobSize, pair)
  }

  @SuppressWarnings(
    Array("org.wartremover.warts.NonUnitStatements"))
  def write(
    dbDir: Path,
    blobSize: Int,
    pair: Pair
  ): Unit = {
    val (_, path) = resolve(dbDir, pair.key)
    Files.write(path, pair.blobStub.generateArray(blobSize))
    ()
  }

  def read(
    dbDir: Path,
    key: Key
  ): Long = {
    val (_, path) = resolve(dbDir, key)
    val array = Files.readAllBytes(path)
    Sum.fromArray(array)
  }

  def resolve(dbDir: Path, key: Key): (Path, Path) = {
    val (start, end) = key.toSplitHex
    val dir = dbDir.resolve(start)
    val path = dir.resolve(end)
    (dir, path)
  }
}
