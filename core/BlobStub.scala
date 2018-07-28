package blobstoreBenchmark.core

import java.nio.ByteBuffer
import scala.util.Random

final case class BlobStub(
  value: Long
) extends AnyVal {
  def generateBuffer(size: Int): ByteBuffer = {
    val buffer = ByteBuffer.allocate(size)
    for (i <- 1 to (size / Key.bytes)) {
      buffer.putLong(value + i)
    }
    buffer.rewind
  }

  def generateArray(size: Int): Array[Byte] =
    generateBuffer(size).array
}

object BlobStub {
  def generate(): BlobStub =
    BlobStub(Random.nextLong())
}
