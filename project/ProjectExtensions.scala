package tacit.sbt

import sbt._

import CustomSettings._
import Dependencies._

object ProjectExtensions {
  implicit final class ProjectHelper(
    val project: Project
  ) extends AnyVal {
    def withCustomSettings() =
      project.settings(allCustomSettings)

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
