package blobstoreBenchmark.core

import java.nio.ByteBuffer
import java.util.Base64
import scala.annotation.tailrec
import scala.collection.immutable.Set
import scala.util.Random

final case class Key(
  value: Long
) extends AnyVal
    with Ordered[Key] {
  def compare(that: Key): Int = value compare that.value

  def toBase64: String =
    Base64.getUrlEncoder.encodeToString(toBytes)

  def toBytes: Array[Byte] =
    toIndirectBuffer.array

  def toIndirectBuffer: ByteBuffer =
    toBuffer(ByteBuffer.allocate(Key.bytes))

  def toBuffer(emptyBuffer: ByteBuffer): ByteBuffer =
    emptyBuffer.putLong(value)
}

object Key {
  val bytes: Int = 8

  @tailrec
  def generate(existingKeys: Set[Key]): Key = {
    val key = Key(Random.nextLong())
    if (existingKeys(key)) {
      generate(existingKeys)
    } else {
      key
    }
  }
}
