package blobstoreBenchmark.core

final case class Pair(
  key: Key,
  blobStub: BlobStub
) {
  def toTuple: (Key, BlobStub) =
    (key, blobStub)
}
