package blobstoreBenchmark.core

import scala.util.Random

object Keys {
  def seed(): Unit =
    Random.setSeed(0)

  def createInitialKeys(count: Int): Array[Long] = {
    val buffer = new Array[Long](count)
    for (i <- 0 to (count - 1)) {
      buffer(i) = Random.nextLong()
    }
    buffer
  }
}
