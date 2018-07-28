package blobstoreBenchmark.core

import java.nio.ByteBuffer
import scala.util.Random

final case class BlobStub(
  value: Long
) extends AnyVal {
  def generateBuffer(
    size: Int,
    buffer: ByteBuffer
  ): ByteBuffer = {
    for (i <- 1 to (size / Key.bytes)) {
      buffer.putLong(value + i)
    }
    buffer
  }

  def generateDirectBuffer(size: Int): ByteBuffer =
    generateBuffer(size, ByteBuffer.allocateDirect(size))

  def generateIndirectBuffer(size: Int): ByteBuffer =
    generateBuffer(size, ByteBuffer.allocate(size))

  def generateArray(size: Int): Array[Byte] =
    generateIndirectBuffer(size).array
}

object BlobStub {
  def generate(): BlobStub =
    BlobStub(Random.nextLong())
}
