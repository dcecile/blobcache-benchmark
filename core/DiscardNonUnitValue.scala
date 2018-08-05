package blobstoreBenchmark.core

object DiscardNonUnitValue {
  // Source: https://stackoverflow.com/a/44689532/207321
  @specialized def discard[A](
    evaluateForSideEffectOnly: A
  ): Unit = {
    val _: A = evaluateForSideEffectOnly
    ()
  }
}
