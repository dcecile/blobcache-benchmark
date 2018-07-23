package blobstoreBenchmark.core

import java.nio.ByteBuffer

object Blob {
  def generate(key: Key, size: Int): Array[Byte] = {
    val buffer = ByteBuffer.allocate(size)
    for (i <- 0 to (size / Key.bytes - 1)) {
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
}
