package blobstoreBenchmark.rocksDB

import java.io.File
import java.nio.ByteBuffer
import org.rocksdb.RocksDB
import org.rocksdb.Options

import blobstoreBenchmark.core.Blob
import blobstoreBenchmark.core.Harness
import blobstoreBenchmark.core.Key
import blobstoreBenchmark.core.Plan
import blobstoreBenchmark.core.Step
import blobstoreBenchmark.core.Verify

object Main extends Harness {
  def init(plan: Plan): Unit =
    withDb(plan.dbDir, db => {
      plan.keys.foreach(write(db, plan.blobSize, _))
    })

  def run(plan: Plan): Unit =
    withDb(plan.dbDir, db => {
      val sum = plan.steps.toStream
        .map(runStep(db, plan, _))
        .sum
      Verify.sum("total", sum, plan.expectedSum)
    })

  def runStep(
    db: RocksDB,
    plan: Plan,
    step: Step
  ): Long = {
    val sum = step.queries.toStream
      .map(read(db, plan.blobSize, _))
      .sum
    step.updates
      .foreach(write(db, plan.blobSize, _))
    step.queries
      .foreach(delete(db, _))
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
    key: Key
  ): Unit = {
    val blob = Blob.generate(key, blobSize)
    db.put(key.toBytes, blob.array)
  }

  def read(
    db: RocksDB,
    blobSize: Int,
    key: Key
  ): Long = {
    val buffer = db.get(key.toBytes)
    Verify.blobSum(ByteBuffer.wrap(buffer), key, blobSize)
  }

  def delete(db: RocksDB, key: Key): Unit =
    db.delete(key.toBytes)
}
