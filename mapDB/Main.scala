package blobstoreBenchmark.mapDB

import java.io.File
import java.lang.{Long => JavaLong}
import java.util.Map
import org.mapdb.DBMaker
import org.mapdb.Serializer

import blobstoreBenchmark.core.Harness
import blobstoreBenchmark.core.Key
import blobstoreBenchmark.core.Pair
import blobstoreBenchmark.core.Plan
import blobstoreBenchmark.core.Step
import blobstoreBenchmark.core.Sum

object Main extends Harness {
  def init(plan: Plan): Unit =
    withMap(plan.dbDir, db => {
      plan.pairs.foreach(write(db, plan.blobSize, _))
    })

  def run(plan: Plan): Long =
    withMap(plan.dbDir, db => {
      plan.steps.toStream
        .map(runStep(db, plan, _))
        .sum
    })

  def runStep(
    db: Map[JavaLong, Array[Byte]],
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

  def withMap[T](
    dbDir: File,
    block: Map[JavaLong, Array[Byte]] => T
  ): T = {
    val db = DBMaker
      .fileDB(new File(dbDir, "store.db"))
      .fileMmapEnable()
      .concurrencyDisable()
      .make()
    val map = db
      .hashMap(
        "pairs",
        Serializer.LONG,
        Serializer.BYTE_ARRAY)
      .createOrOpen()
    val result = block(map)
    db.commit()
    db.close()
    result
  }

  def write(
    db: Map[JavaLong, Array[Byte]],
    blobSize: Int,
    pair: Pair
  ): Unit = {
    val _ = db.put(
      pair.key.value,
      pair.blobStub.generateArray(blobSize))
  }

  def read(
    db: Map[JavaLong, Array[Byte]],
    key: Key
  ): Long = {
    val array = db.get(key.value)
    Sum.fromArray(array)
  }
}
