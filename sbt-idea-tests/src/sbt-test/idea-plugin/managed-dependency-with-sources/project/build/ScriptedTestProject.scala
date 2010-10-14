import sbt._

class ScriptedTestProject(info: ProjectInfo) extends DefaultProject(info) with ScriptedTestAssertTasks with IdeaProject {
  val commons = "commons-io" % "commons-io" % "1.4" withSources() 
}
