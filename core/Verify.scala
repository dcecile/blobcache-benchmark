package blobstoreBenchmark.core

object Verify {
  def blobSum(
    blob: Array[Byte],
    key: Key,
    size: Int): Long = {
    val actual = Blob.sum(blob)
    val expected = Blob.predictSum(key, size)
    sum(s"key ${key.toBase64}", actual, expected)
    actual
  }

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def sum(
    description: String,
    actual: Long,
    expected: Long): Unit =
    if (actual != expected) {
      throw new Exception(
        s"${description} sum failure: actual ${actual.toHexString}, expected ${expected.toHexString}")
    }
}
