package blobstoreBenchmark.core

import java.nio.ByteBuffer

object Blob {
  def fromKey(key: Long, size: Int): Array[Byte] = {
    val buffer = ByteBuffer.allocate(size)
    for (i <- 0 to (size / 8 - 1)) {
      buffer.putLong(key + i)
    }
    buffer.array
  }
}
