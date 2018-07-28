package blobstoreBenchmark.core

object Verify {
  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def sum(
    description: String,
    actual: Long,
    expected: Long
  ): Unit =
    if (actual != expected) {
      throw new Exception(
        s"${description} sum failure: actual ${actual.toHexString}, expected ${expected.toHexString}")
    }
}
