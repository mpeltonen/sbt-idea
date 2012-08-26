import org.sbtidea.test.util.AbstractScriptedTestBuild
import sbt._
import sbt.Keys._

object ScriptedTestBuild extends AbstractScriptedTestBuild("external-rootproject-dependency") {

  lazy val mainSettings: Seq[Project.Setting[_]] = Defaults.defaultSettings ++ scriptedTestSettings ++ Seq(
    scalacOptions ++= Seq("-unchecked")
  )

  lazy val root = Project("root-project", file("."), settings = mainSettings) aggregate(dependencyProject) dependsOn(dependencyProject)
  lazy val dependencyProject = RootProject(file("dependency-project"))
}
