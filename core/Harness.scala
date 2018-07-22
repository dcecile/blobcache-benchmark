package blobstoreBenchmark.core

import java.io.File
import org.apache.commons.io.FileUtils

object Harness {
  def makeEmptyDbDir(): File = {
    val dbDir = getDbDir()
    if (dbDir.exists()) {
      FileUtils.deleteDirectory(dbDir)
    }
    FileUtils.forceMkdir(dbDir)
    dbDir
  }

  def getDbDir(): File =
    new File("db")
}
