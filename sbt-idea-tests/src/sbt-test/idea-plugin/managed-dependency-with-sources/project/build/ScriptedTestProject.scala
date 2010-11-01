import sbt._

class ScriptedTestProject(info: ProjectInfo) extends DefaultProject(info) with ScriptedTestAssertTasks with IdeaProject {
  val commons = "commons-io" % "commons-io" % "1.4" withSources() extra("docUrl" -> "http://commons.apache.org/io/api-1.4/")
}
