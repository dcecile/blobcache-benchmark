package blobstoreBenchmark.core

import java.nio.ByteBuffer
import java.util.Base64
import scala.annotation.tailrec
import scala.collection.immutable.Set
import scala.util.Random

final case class Key(
  value: Long
) extends AnyVal {
  def toBase64: String =
    Base64.getUrlEncoder.encodeToString(toBytes)

  def toBytes: Array[Byte] =
    ByteBuffer.allocate(Key.bytes).putLong(value).array
}

object Key {
  val bytes: Int = 8

  @tailrec
  def generate(existingKeys: Set[Long]): Key = {
    val value = Random.nextLong()
    if (existingKeys(value)) {
      generate(existingKeys)
    } else {
      Key(value)
    }
  }
}
