package blobstoreBenchmark.core

import scala.annotation.tailrec
import scala.collection.immutable.TreeSet
import scala.util.Random

final case class Step(
  updates: List[Pair]
) extends AnyVal {
  def queries: List[Key] = updates.map(_.key)
}

object Step {
  def generate(
    vector: Vector[Key],
    stepSize: Int
  ): Step = {
    val queries = pickQueries(
      List[Key](),
      TreeSet[Key](),
      vector,
      stepSize)
    val updates =
      pickUpdates(queries)
    Step(updates)
  }

  @tailrec
  private def pickQueries(
    queries: List[Key],
    querySet: TreeSet[Key],
    vector: Vector[Key],
    remaining: Int
  ): List[Key] = {
    val key = pickQuery(querySet, vector)
    val newQueries = key +: queries
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
    querySet: TreeSet[Key],
    vector: Vector[Key]
  ): Key = {
    val i = Random.nextInt(vector.size)
    val key = vector(i)
    if (querySet(key)) {
      pickQuery(querySet, vector)
    } else {
      key
    }
  }

  private def pickUpdates(
    queries: List[Key]
  ): List[Pair] =
    queries.map(key => Pair(key, BlobStub.generate()))
}
