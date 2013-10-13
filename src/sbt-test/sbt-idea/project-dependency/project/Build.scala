import org.sbtidea.test.util.AbstractScriptedTestBuild
import sbt._

object ScriptedTestBuild extends AbstractScriptedTestBuild("simple-project") {

  lazy val root = Project("main", file("."), settings = Defaults.defaultSettings ++ scriptedTestSettings)
    .aggregate(compileProject, providedProject, runtimeProject, testProject)
    .dependsOn(compileProject)
    .dependsOn(providedProject % "provided")
    .dependsOn(runtimeProject % "runtime")
    .dependsOn(testProject % "test")

  lazy val compileProject = Project("compile-project", file("compile-project"))
  lazy val providedProject = Project("provided-project", file("provided-project"))
  lazy val runtimeProject = Project("runtime-project", file("runtime-project"))
  lazy val testProject = Project("test-project", file("test-project"))
}
