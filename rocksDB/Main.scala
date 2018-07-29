package blobstoreBenchmark.rocksDB

import java.io.File
import org.rocksdb.RocksDB
import org.rocksdb.Options
import org.rocksdb.WriteBatch
import org.rocksdb.WriteOptions

import blobstoreBenchmark.core.Harness
import blobstoreBenchmark.core.Key
import blobstoreBenchmark.core.Pair
import blobstoreBenchmark.core.Plan
import blobstoreBenchmark.core.Step
import blobstoreBenchmark.core.Sum

object Main extends Harness {
  def init(plan: Plan): Unit =
    withDb(plan.dbDir, db => {
      plan.pairs
        .grouped(100)
        .foreach(writeBatch(db, plan.blobSize, _))
    })

  def run(plan: Plan): Long =
    withDb(plan.dbDir, db => {
      plan.steps.toStream
        .map(runStep(db, plan, _))
        .sum
    })

  def runStep(
    db: RocksDB,
    plan: Plan,
    step: Step
  ): Long = {
    val sum = step.queries.toStream
      .map(read(db, _))
      .sum
    writeBatch(db, plan.blobSize, step.updates)
    sum
  }

  def withDb[T](dbDir: File, block: RocksDB => T): T = {
    val options = new Options()
      .setCreateIfMissing(true)
      .setAllowMmapReads(true)
      .setAllowMmapWrites(true)
    val db = RocksDB.open(
      options,
      new File(dbDir, "store").getPath())
    val result = block(db)
    db.close()
    options.close()
    result
  }

  def writeBatch(
    db: RocksDB,
    blobSize: Int,
    pairs: List[Pair]
  ): Unit = {
    val batch = new WriteBatch()
    pairs.foreach(write(batch, blobSize, _))
    val options = new WriteOptions()
      .setSync(false)
      .setDisableWAL(true)
    db.write(options, batch)
    batch.close()
  }

  def write(
    batch: WriteBatch,
    blobSize: Int,
    pair: Pair
  ): Unit =
    batch.put(
      pair.key.toArray,
      pair.blobStub.generateArray(blobSize))

  def read(
    db: RocksDB,
    key: Key
  ): Long = {
    val array = db.get(key.toArray)
    Sum.fromArray(array)
  }
}
