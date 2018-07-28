package blobstoreBenchmark.rocksDB

import java.io.File
import org.rocksdb.RocksDB
import org.rocksdb.Options

import blobstoreBenchmark.core.Harness
import blobstoreBenchmark.core.Key
import blobstoreBenchmark.core.Pair
import blobstoreBenchmark.core.Plan
import blobstoreBenchmark.core.Step
import blobstoreBenchmark.core.Sum

object Main extends Harness {
  def init(plan: Plan): Unit =
    withDb(plan.dbDir, db => {
      plan.pairs.foreach(write(db, plan.blobSize, _))
    })

  def run(plan: Plan): Long =
    withDb(
      plan.dbDir,
      db =>
        plan.steps.toStream
          .map(runStep(db, plan, _))
          .sum)

  def runStep(
    db: RocksDB,
    plan: Plan,
    step: Step
  ): Long = {
    val sum = step.queries.toStream
      .map(read(db, _))
      .sum
    step.updates
      .foreach(write(db, plan.blobSize, _))
    sum
  }

  def withDb[T](dbDir: File, block: RocksDB => T): T = {
    val options = new Options().setCreateIfMissing(true)
    val db = RocksDB.open(
      options,
      new File(dbDir, "store").getPath())
    val result = block(db)
    db.close()
    options.close()
    result
  }

  def write(
    db: RocksDB,
    blobSize: Int,
    pair: Pair
  ): Unit =
    db.put(
      pair.key.toBytes,
      pair.blobStub.generateArray(blobSize))

  def read(
    db: RocksDB,
    key: Key
  ): Long = {
    val array = db.get(key.toBytes)
    Sum.fromArray(array)
  }
}
