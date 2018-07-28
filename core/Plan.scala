package blobstoreBenchmark.core

import java.io.File
import scala.annotation.tailrec
import scala.collection.immutable.TreeMap
import scala.collection.immutable.TreeSet
import scala.util.Random

final case class Plan(
  dbDir: File,
  keyCount: Int,
  blobSize: Int,
  stepCount: Int,
  stepSize: Int,
  pairs: List[Pair],
  steps: List[Step],
  expectedSum: Long)

object Plan {
  def generate(
    dbDir: File,
    keyCount: Int,
    stepCount: Int
  ): Plan = {
    seed()

    val stepSize = 10
    val blobSize = 4096

    val (pairs, set) =
      generatePairs(keyCount)
    val steps =
      generateSteps(set, stepCount, stepSize)
    val expectedSum =
      predictSum(pairs, steps, blobSize)

    new Plan(
      dbDir,
      keyCount,
      blobSize,
      stepCount,
      stepSize,
      pairs,
      steps,
      expectedSum)
  }

  private def seed(): Unit =
    Random.setSeed(0)

  private def generatePairs(
    keyCount: Int
  ): (List[Pair], TreeSet[Key]) = {
    @tailrec
    def loop(
      pairs: List[Pair],
      set: TreeSet[Key],
      remaining: Int
    ): (List[Pair], TreeSet[Key]) = {
      val key = Key.generate(set)
      val pair = Pair(key, BlobStub.generate())
      val newPairs = pair +: pairs
      val newSet = set + key
      if (remaining > 0) {
        loop(newPairs, newSet, remaining - 1)
      } else {
        (newPairs.reverse, newSet)
      }
    }
    loop(List[Pair](), TreeSet[Key](), keyCount)
  }

  private def generateSteps(
    set: TreeSet[Key],
    stepCount: Int,
    stepSize: Int
  ) = {
    val vector = set.toVector
    (1 to stepCount).toList
      .map(_ => Step.generate(vector, stepSize))
  }

  private def predictSum(
    pairs: List[Pair],
    steps: List[Step],
    blobSize: Int
  ): Long =
    steps.toStream
      .flatMap(_.updates)
      .foldLeft((0L, TreeMap(pairs.map(_.toTuple): _*)))(
        predictUpdate(blobSize, _, _))
      ._1

  private def predictUpdate(
    blobSize: Int,
    input: (Long, TreeMap[Key, BlobStub]),
    pair: Pair
  ): (Long, TreeMap[Key, BlobStub]) = {
    val (sum, state) = input
    (
      sum + Sum.predict(state(pair.key), blobSize),
      state.updated(pair.key, pair.blobStub))
  }
}
