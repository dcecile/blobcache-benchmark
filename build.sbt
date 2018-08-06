import blobstoreBenchmark.sbt.BenchTask._
import blobstoreBenchmark.sbt.Dependencies._
import blobstoreBenchmark.sbt.ProjectExtensions._

lazy val core =
  (project in file("core"))
    .withCustomSettings()
    .libraryDependencies(
      jna,
      commonsIo,
      scallop,
      purecsv
    )

lazy val simpleFileStream =
  (project in file("simpleFileStream"))
    .withCustomSettings()
    .withBenchSettings()
    .libraryDependencies()
    .projectDependencies(
      core
    )

lazy val simpleFiles =
  (project in file("simpleFiles"))
    .withCustomSettings()
    .withBenchSettings()
    .libraryDependencies()
    .projectDependencies(
      core
    )

lazy val simpleSplitFiles =
  (project in file("simpleSplitFiles"))
    .withCustomSettings()
    .withBenchSettings()
    .libraryDependencies()
    .projectDependencies(
      core
    )

lazy val monixNio =
  (project in file("monixNio"))
    .withCustomSettings()
    .withBenchSettings()
    .libraryDependencies(
      monix,
      monixnio
    )
    .projectDependencies(
      core
    )

lazy val fs2 =
  (project in file("fs2"))
    .withCustomSettings()
    .withBenchSettings()
    .libraryDependencies(
      fs2io
    )
    .projectDependencies(
      core
    )

lazy val h2 =
  (project in file("h2"))
    .withCustomSettings()
    .withBenchSettings()
    .libraryDependencies(
      h2database
    )
    .projectDependencies(
      core
    )

lazy val sqlite =
  (project in file("sqlite"))
    .withCustomSettings()
    .withBenchSettings()
    .libraryDependencies(
      sqlitejdbc
    )
    .projectDependencies(
      core
    )

lazy val rocksDB =
  (project in file("rocksDB"))
    .withCustomSettings()
    .withBenchSettings()
    .libraryDependencies(
      rocksdbjni
    )
    .projectDependencies(
      core
    )

lazy val lmdb =
  (project in file("lmdb"))
    .withCustomSettings()
    .withBenchSettings()
    .libraryDependencies(
      lmdbjava
    )
    .projectDependencies(
      core
    )

lazy val mapDB =
  (project in file("mapDB"))
    .withCustomSettings()
    .withBenchSettings()
    .libraryDependencies(
      mapdb
    )
    .projectDependencies(
      core
    )

lazy val xodus =
  (project in file("xodus"))
    .withCustomSettings()
    .withBenchSettings()
    .libraryDependencies(
      xodusenv
    )
    .projectDependencies(
      core
    )

lazy val report =
  (project in file("report"))
    .withCustomSettings()
    .libraryDependencies(
      scallop,
      evilplot
    )
    .projectDependencies(
      core
    )

lazy val root =
  (project in file("."))
    .aggregate(
      core,
      simpleFileStream,
      simpleFiles,
      simpleSplitFiles,
      monixNio,
      fs2,
      h2,
      sqlite,
      rocksDB,
      lmdb,
      mapDB,
      xodus,
      report
    )
    .withRootBenchSettings(
      simpleFileStream,
      simpleFiles,
      simpleSplitFiles,
      monixNio,
      fs2,
      h2,
      sqlite,
      rocksDB,
      lmdb,
      mapDB,
      xodus
    )

Global / fork := true
Global / outputStrategy := Some(StdoutOutput)
Global / cancelable := true
