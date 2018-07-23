package blobstoreBenchmark.core

import scala.annotation.tailrec
import scala.collection.immutable.TreeSet
import scala.util.Random

final case class Plan(
  keyCount: Int,
  blobSize: Int,
  stepCount: Int,
  stepSize: Int,
  keys: List[Key],
  steps: List[Step])

object Plan {
  def generate(): Plan = {
    seed()

    val keyCount = 100
    val stepCount = 1000
    val stepSize = 10
    val blobSize = 4096

    val (keys, set) =
      generateKeys(List[Key](), TreeSet[Long](), keyCount)
    val steps =
      generateSteps(
        List[Step](),
        set,
        set.toVector,
        stepSize,
        stepCount)

    new Plan(
      keyCount,
      blobSize,
      stepCount,
      stepSize,
      keys,
      steps)
  }

  private def seed(): Unit =
    Random.setSeed(0)

  @tailrec
  private def generateKeys(
    keys: List[Key],
    set: TreeSet[Long],
    remaining: Int
  ): (List[Key], TreeSet[Long]) = {
    val key = Key.generate(set)
    val newKeys = key +: keys
    val newSet = set + key.value
    if (remaining > 0) {
      generateKeys(newKeys, newSet, remaining - 1)
    } else {
      (newKeys.reverse, newSet)
    }
  }

  @tailrec
  private def generateSteps(
    steps: List[Step],
    set: TreeSet[Long],
    vector: Vector[Long],
    stepSize: Int,
    remaining: Int
  ): List[Step] = {
    val (step, newSet, newVector) =
      Step.generate(set, vector, stepSize)
    val newSteps = step +: steps
    if (remaining > 0) {
      generateSteps(
        newSteps,
        newSet,
        newVector,
        stepSize,
        remaining - 1)
    } else {
      newSteps.reverse
    }
  }
}