import org.sbtidea.test.util.AbstractScriptedTestBuild
import sbt._
import Keys.libraryDependencies

object ScriptedTestBuild extends AbstractScriptedTestBuild("external-rootproject-dependency") {
  lazy val root = Project("root-project", file("."), settings = Defaults.defaultSettings ++ scriptedTestSettings) aggregate(dependencyProject) dependsOn(dependencyProject)
  lazy val dependencyProject = RootProject(file("dependency-project"))
}
