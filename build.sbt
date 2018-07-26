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

lazy val root =
  (project in file("."))
    .aggregate(
      core,
      syncFS,
      asyncFS
    )

fork in Global := true
outputStrategy in Global := Some(StdoutOutput)
cancelable in Global := true
