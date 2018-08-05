package blobstoreBenchmark.lmdb

import java.io.File
import java.nio.ByteBuffer
import org.lmdbjava.Env
import org.lmdbjava.EnvFlags
import org.lmdbjava.Dbi
import org.lmdbjava.DbiFlags
import org.lmdbjava.Txn

import blobstoreBenchmark.core.DiscardNonUnitValue.discard
import blobstoreBenchmark.core.BlobStub
import blobstoreBenchmark.core.Harness
import blobstoreBenchmark.core.Key
import blobstoreBenchmark.core.Pair
import blobstoreBenchmark.core.Plan
import blobstoreBenchmark.core.Step
import blobstoreBenchmark.core.Sum

object Main extends Harness {
  def init(plan: Plan): Unit =
    withDb(plan.dbDir, (env, db) => {
      val txn = env.txnWrite()
      plan.pairs
        .grouped(100)
        .foreach(group => {
          group.foreach(write(txn, db, _))
        })
      txn.commit()
      txn.close()
    })

  def run(plan: Plan): Long =
    withDb(plan.dbDir, (env, db) => {
      val txn = env.txnWrite()
      val sum = plan.steps.toStream
        .map(runStep(txn, db, _))
        .sum
      txn.commit()
      txn.close()
      sum
    })

  def runStep(
    txn: Txn[ByteBuffer],
    db: Dbi[ByteBuffer],
    step: Step
  ): Long = {
    val sum = step.queries.toStream
      .map(read(txn, db, _))
      .sum
    step.updates
      .foreach(write(txn, db, _))
    sum
  }

  def withDb[T](
    dbDir: File,
    block: (Env[ByteBuffer], Dbi[ByteBuffer]) => T
  ): T = {
    val env = Env
      .create()
      .setMapSize(500 * 1000 * 1000)
      .setMaxDbs(1)
      .open(dbDir, EnvFlags.MDB_WRITEMAP, EnvFlags.MDB_NOSYNC)
    val db = env.openDbi(
      "store",
      DbiFlags.MDB_CREATE,
      DbiFlags.MDB_INTEGERKEY)
    val result = block(env, db)
    db.close()
    env.close()
    result
  }

  def write(
    txn: Txn[ByteBuffer],
    db: Dbi[ByteBuffer],
    pair: Pair
  ): Unit =
    discard(
      db.put(
        txn,
        writeKeyBuffer(pair.key),
        writeValueBuffer(pair.blobStub)))

  def read(
    txn: Txn[ByteBuffer],
    db: Dbi[ByteBuffer],
    key: Key
  ): Long = {
    val valueBuffer = db.get(txn, writeKeyBuffer(key))
    Sum.fromBuffer(valueBuffer)
  }

  val keyBuffer: ByteBuffer = ByteBuffer.allocateDirect(511) // AKA env.getMaxKeySize()
  val valueBuffer: ByteBuffer =
    ByteBuffer.allocateDirect(4096) // AKA plan.blobSize

  def writeKeyBuffer(key: Key): ByteBuffer =
    key.toBuffer(keyBuffer.clear).flip

  def writeValueBuffer(blobStub: BlobStub): ByteBuffer =
    blobStub.generateBuffer(4096, valueBuffer.clear).flip
}
