import sbt._

class ScriptedTestProject(info: ProjectInfo) extends DefaultProject(info) with BasicScalaIntegrationTesting with ScriptedTestAssertTasks with IdeaProject {
  override def mainSourceRoots = super.mainSourceRoots +++ mainSourcePath / "other"
}