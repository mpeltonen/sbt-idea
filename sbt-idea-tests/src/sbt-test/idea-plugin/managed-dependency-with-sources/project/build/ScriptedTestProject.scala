import sbt._
import IdeaDocUrl._

class ScriptedTestProject(info: ProjectInfo) extends DefaultProject(info) with ScriptedTestAssertTasks with IdeaProject {
  val commons = "commons-io" % "commons-io" % "1.4" withSources() ideaDocUrl("http://commons.apache.org/io/api-1.4/")
}
