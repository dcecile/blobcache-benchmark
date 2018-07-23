package blobstoreBenchmark.core

import java.nio.ByteBuffer

object Blob {
  def generate(key: Key, size: Int): Array[Byte] = {
    val buffer = ByteBuffer.allocate(size)
    for (i <- 1 to (size / Key.bytes)) {
      buffer.putLong(key.value + i)
    }
    buffer.array
  }

  def sum(blob: Array[Byte]): Long = {
    val buffer = ByteBuffer.wrap(blob)
    (1 to (blob.size / Key.bytes)).toStream
      .map(_ => buffer.getLong())
      .sum
  }

  def predictSum(key: Key, size: Int): Long = {
    val count = (size / Key.bytes).toLong
    val constant = key.value * count
    val series = count * (1 + count) / 2
    constant + series
  }
}
