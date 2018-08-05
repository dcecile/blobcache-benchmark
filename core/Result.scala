package blobstoreBenchmark.core

final case class Result(
  blobstore: String,
  keyCount: Int,
  userSeconds: Double,
  systemSeconds: Double,
  totalSeconds: Double)
