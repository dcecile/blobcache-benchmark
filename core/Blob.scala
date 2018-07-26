package blobstoreBenchmark.core

import java.nio.ByteBuffer

object Blob {
  def generate(key: Key, size: Int): ByteBuffer = {
    val buffer = ByteBuffer.allocate(size)
    for (i <- 1 to (size / Key.bytes)) {
      buffer.putLong(key.value + i)
    }
    buffer.rewind
  }

  def sum(blob: ByteBuffer): Long =
    (1 to (blob.remaining / Key.bytes)).toStream
      .map(_ => blob.getLong())
      .sum

  def predictSum(key: Key, size: Int): Long = {
    val count = (size / Key.bytes).toLong
    val constant = key.value * count
    val series = count * (1 + count) / 2
    constant + series
  }
}
