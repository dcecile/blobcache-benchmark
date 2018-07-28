package blobstoreBenchmark.core

import java.nio.ByteBuffer

object Sum {
  def fromBuffer(buffer: ByteBuffer): Long =
    (1 to (buffer.remaining / Key.bytes)).toStream
      .map(_ => buffer.getLong())
      .sum

  def fromArray(array: Array[Byte]): Long =
    fromBuffer(ByteBuffer.wrap(array))

  def predict(blobStub: BlobStub, size: Int): Long = {
    val count = (size / Key.bytes).toLong
    val constant = blobStub.value * count
    val series = count * (1 + count) / 2
    constant + series
  }
}
