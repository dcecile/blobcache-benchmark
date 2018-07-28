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

lazy val syncFS =
  (project in file("syncFS"))
    .withCustomSettings()
    .libraryDependencies()
    .projectDependencies(
      core
    )

lazy val asyncFS =
  (project in file("asyncFS"))
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

lazy val root =
  (project in file("."))
    .aggregate(
      core,
      syncFS,
      asyncFS,
      h2,
      rocksDB,
      lmdb
    )

fork in Global := true
outputStrategy in Global := Some(StdoutOutput)
cancelable in Global := true
