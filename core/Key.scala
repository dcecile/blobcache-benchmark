package blobstoreBenchmark.core

import java.util.Base64
import java.nio.ByteBuffer
import scala.util.Random

object Key {
  def seed(): Unit =
    Random.setSeed(0)

  def createInitialKeys(count: Int): Array[Long] = {
    val buffer = new Array[Long](count)
    for (i <- 0 to (count - 1)) {
      buffer(i) = Random.nextLong()
    }
    buffer
  }

  def toBase64(key: Long): String =
    Base64.getUrlEncoder.encodeToString(toBytes(key))

  def toBytes(key: Long): Array[Byte] =
    ByteBuffer.allocate(8).putLong(key).array
}
