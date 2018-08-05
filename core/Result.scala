package blobstoreBenchmark.core

import boopickle.Default._
import java.nio.file.Files
import java.nio.ByteBuffer
import java.nio.file.Path
import java.nio.file.Paths

import DiscardNonUnitValue.discard

final case class Result(
  blobstore: String,
  keyCount: Int,
  userSeconds: Double,
  systemSeconds: Double,
  totalSeconds: Double)

object Result {
  @SuppressWarnings(
    Array(
      "org.wartremover.warts.ImplicitParameter",
      "org.wartremover.warts.NonUnitStatements",
      "org.wartremover.warts.OptionPartial"))
  def loadAll(): Seq[Result] =
    if (Files.exists(path)) {
      val bytes = Files.readAllBytes(path)
      val buffer = ByteBuffer.wrap(bytes)
      Unpickle[Seq[Result]].fromBytes(buffer)
    } else {
      Seq()
    }

  @SuppressWarnings(
    Array(
      "org.wartremover.warts.ImplicitParameter",
      "org.wartremover.warts.NonUnitStatements",
      "org.wartremover.warts.OptionPartial"))
  def saveAll(results: Seq[Result]): Unit = {
    val buffer = Pickle.intoBytes(results)
    discard(Files.write(path, buffer.array))
  }

  def clearAll(): Unit =
    discard(Files.deleteIfExists(path))

  val path: Path = Paths.get("..", "results.bin")
}
