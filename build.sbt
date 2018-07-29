import tacit.sbt.Dependencies._
import tacit.sbt.ProjectExtensions._

lazy val core =
  (project in file("core"))
    .withCustomSettings()
    .libraryDependencies(
      jna,
      commonsIo,
      scallop
    )

lazy val simpleFileStream =
  (project in file("simpleFileStream"))
    .withCustomSettings()
    .libraryDependencies()
    .projectDependencies(
      core
    )

lazy val monixNio =
  (project in file("monixNio"))
    .withCustomSettings()
    .libraryDependencies(
      monix,
      monixnio
    )
    .projectDependencies(
      core
    )

lazy val h2 =
  (project in file("h2"))
    .withCustomSettings()
    .libraryDependencies(
      h2database
    )
    .projectDependencies(
      core
    )

lazy val rocksDB =
  (project in file("rocksDB"))
    .withCustomSettings()
    .libraryDependencies(
      rocksdbjni
    )
    .projectDependencies(
      core
    )

lazy val lmdb =
  (project in file("lmdb"))
    .withCustomSettings()
    .libraryDependencies(
      lmdbjava
    )
    .projectDependencies(
      core
    )

lazy val mapDB =
  (project in file("mapDB"))
    .withCustomSettings()
    .libraryDependencies(
      mapdb
    )
    .projectDependencies(
      core
    )

lazy val xodus =
  (project in file("xodus"))
    .withCustomSettings()
    .libraryDependencies(
      xodusenv
    )
    .projectDependencies(
      core
    )

lazy val root =
  (project in file("."))
    .aggregate(
      core,
      simpleFileStream,
      monixNio,
      h2,
      rocksDB,
      lmdb,
      mapDB,
      xodus
    )

fork in Global := true
outputStrategy in Global := Some(StdoutOutput)
cancelable in Global := true
