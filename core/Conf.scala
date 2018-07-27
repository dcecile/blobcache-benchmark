package blobstoreBenchmark.core

import org.rogach.scallop._

class Conf(arguments: Seq[String])
    extends ScallopConf(arguments) {
  object clean extends Subcommand("clean")
  addSubcommand(clean)

  object init extends Subcommand("init") {
    val keyCount: ScallopOption[Int] = trailArg[Int]()
    val ignored: ScallopOption[Int] =
      trailArg[Int](required = false)
  }
  addSubcommand(init)

  object run extends Subcommand("run") {
    val keyCount: ScallopOption[Int] = trailArg[Int]()
    val stepCount: ScallopOption[Int] = trailArg[Int]()
  }
  addSubcommand(run)

  verify()
}
