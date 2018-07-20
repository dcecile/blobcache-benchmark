import tacit.sbt.Dependencies._
import tacit.sbt.ProjectExtensions._

lazy val core =
  (project in file("core"))
    .withCustomSettings()

lazy val syncFS =
  (project in file("syncFS"))
    .withCustomSettings()
    .libraryDependencies(
      nameof
    )
    .projectDependencies(
      core
    )

lazy val root =
  (project in file("."))
    .aggregate(
      core,
      syncFS
    )

cancelable in Global := true
