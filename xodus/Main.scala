package blobstoreBenchmark.xodus

import java.io.File
import java.nio.ByteBuffer
import jetbrains.exodus.ArrayByteIterable
import jetbrains.exodus.env.Environments
import jetbrains.exodus.env.Store
import jetbrains.exodus.env.StoreConfig
import jetbrains.exodus.env.Transaction
import jetbrains.exodus.env.TransactionalComputable

import blobstoreBenchmark.core.Harness
import blobstoreBenchmark.core.Key
import blobstoreBenchmark.core.Pair
import blobstoreBenchmark.core.Plan
import blobstoreBenchmark.core.Step
import blobstoreBenchmark.core.Sum

object Main extends Harness {
  def init(plan: Plan): Unit =
    withTxn(plan.dbDir, (store, txn) => {
      plan.pairs.foreach(write(store, txn, plan.blobSize, _))
    })

  def run(plan: Plan): Long =
    withTxn(plan.dbDir, (store, txn) => {
      plan.steps.toStream
        .map(runStep(store, txn, plan, _))
        .sum
    })

  def runStep(
    store: Store,
    txn: Transaction,
    plan: Plan,
    step: Step
  ): Long = {
    val sum = step.queries.toStream
      .map(read(store, txn, _))
      .sum
    step.updates
      .foreach(write(store, txn, plan.blobSize, _))
    sum
  }

  def withTxn[T](
    dbDir: File,
    block: (Store, Transaction) => T
  ): T = {
    val env = Environments.newInstance(dbDir)
    val store = env.computeInTransaction(
      new TransactionalComputable[Store] {
        def compute(txn: Transaction): Store =
          env.openStore(
            "MyStore",
            StoreConfig.WITHOUT_DUPLICATES,
            txn);
      });
    val result = env.computeInTransaction(
      new TransactionalComputable[T] {
        def compute(txn: Transaction): T =
          block(store, txn)
      });
    env.close()
    result
  }

  def write(
    store: Store,
    txn: Transaction,
    blobSize: Int,
    pair: Pair
  ): Unit = {
    val _ = store.put(
      txn,
      new ArrayByteIterable(pair.key.toArray),
      new ArrayByteIterable(
        pair.blobStub.generateArray(blobSize)))
  }

  @SuppressWarnings(
    Array(
      "org.wartremover.warts.While",
      "org.wartremover.warts.NonUnitStatements"))
  def read(
    store: Store,
    txn: Transaction,
    key: Key
  ): Long = {
    val iterable =
      store.get(txn, new ArrayByteIterable(key.toArray))
    val iterator = iterable.iterator
    val buffer = ByteBuffer.allocate(iterable.getLength())
    while (iterator.hasNext()) {
      buffer.putLong(iterator.nextLong(Key.bytes))
    }
    Sum.fromBuffer(buffer.flip)
  }
}
