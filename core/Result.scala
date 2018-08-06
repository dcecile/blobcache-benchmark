package blobstoreBenchmark.core

import purecsv.unsafe._
import java.io.File
import java.nio.file.Files

import DiscardNonUnitValue.discard

final case class Result(
  blobstore: String,
  keyCount: Int,
  userSeconds: Double,
  systemSeconds: Double,
  totalSeconds: Double,
  totalSizeMegabytes: Double)

object Result {
  def loadAll(): Seq[Result] =
    if (file.exists()) {
      CSVReader[Result].readCSVFromFile(file)
    } else {
      Seq()
    }

  def saveAll(results: Seq[Result]): Unit =
    results.writeCSVToFile(file)

  def clearAll(): Unit =
    discard(Files.deleteIfExists(file.toPath))

  val file: File = new File("../results.csv")
}
