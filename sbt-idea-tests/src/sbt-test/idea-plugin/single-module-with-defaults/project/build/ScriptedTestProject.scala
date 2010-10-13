import sbt._

class ScriptedTestProject(info: ProjectInfo) extends DefaultProject(info) with ScriptedTestAssertTasks with IdeaProject
