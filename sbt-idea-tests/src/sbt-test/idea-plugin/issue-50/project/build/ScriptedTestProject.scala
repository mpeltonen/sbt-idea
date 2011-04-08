import sbt._

class ScriptedTestProject(info: ProjectInfo) extends DefaultProject(info) with ScriptedTestAssertTasks with IdeaProject {
  val slf4j = "org.slf4j" % "slf4j-api" % "1.6.0"
  val slf4jsimple = "org.slf4j" % "slf4j-simple" % "1.6.0" % "test"
}
