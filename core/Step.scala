package blobstoreBenchmark.core

import scala.annotation.tailrec
import scala.collection.immutable.TreeSet
import scala.util.Random

final case class Step(
  keys: List[(Key, Key)]
) {
  def queries: List[Key] = keys.map(_._1)
  def updates: List[Key] = keys.map(_._2)
}

object Step {
  def generate(
    set: TreeSet[Long],
    vector: Vector[Long],
    stepSize: Int
  ): (Step, TreeSet[Long], Vector[Long]) = {
    val queries = pickQueries(
      List[(Long, Int)](),
      TreeSet[Long](),
      vector,
      stepSize)
    val (updates, newSet, newVector) =
      pickUpdates(List[(Long, Long)](), set, vector, queries)
    val keys = updates.map({
      case (query, update) => (Key(query), Key(update))
    })
    (Step(keys), newSet, newVector)
  }

  @tailrec
  private def pickQueries(
    queries: List[(Long, Int)],
    querySet: TreeSet[Long],
    vector: Vector[Long],
    remaining: Int
  ): List[(Long, Int)] = {
    val (key, i) = pickQuery(querySet, vector)
    val newQueries = (key, i) +: queries
    val newQuerySet = querySet + key
    if (remaining > 0) {
      pickQueries(
        newQueries,
        newQuerySet,
        vector,
        remaining - 1)
    } else {
      newQueries.reverse
    }
  }

  @tailrec
  private def pickQuery(
    querySet: TreeSet[Long],
    vector: Vector[Long]
  ): (Long, Int) = {
    val i = Random.nextInt(vector.size)
    val key = vector(i)
    if (querySet(key)) {
      pickQuery(querySet, vector)
    } else {
      (key, i)
    }
  }

  @tailrec
  private def pickUpdates(
    updates: List[(Long, Long)],
    set: TreeSet[Long],
    vector: Vector[Long],
    remaining: List[(Long, Int)]
  ): (List[(Long, Long)], TreeSet[Long], Vector[Long]) =
    remaining match {
      case (key, i) +: newRemaining => {
        val newKey = Key.generate(set).value
        val newUpdates = (key, newKey) +: updates
        val newSet = set + newKey
        val newVector = vector.updated(i, newKey)
        pickUpdates(
          newUpdates,
          newSet,
          newVector,
          newRemaining)
      }
      case Nil =>
        (updates.reverse, set -- updates.map(_._1), vector)
    }
}
