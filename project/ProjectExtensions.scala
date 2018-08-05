package blobstoreBenchmark.sbt

import sbt._

import BenchTask._
import CustomSettings._
import Dependencies._

object ProjectExtensions {
  implicit final class ProjectHelper(
    val project: Project
  ) extends AnyVal {
    def withCustomSettings() =
      project.settings(allCustomSettings)

    def withBenchSettings() =
      project.settings(benchOneTaskSettings)

    def withRootBenchSettings(projects: ProjectReference*) =
      project.settings(benchTaskSettings(projects))

    def libraryDependencies(
      dependencies: Def.Initialize[ModuleID]*
    ) =
      project.settings(
        toSettings(dependencies)
      )

    def projectDependencies(
      dependencies: ClasspathDep[ProjectReference]*
    ) =
      project.dependsOn(dependencies: _*)
  }
}
