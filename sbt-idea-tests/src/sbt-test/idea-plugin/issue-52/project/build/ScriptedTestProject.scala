import sbt._

class ScriptedTestProject(info: ProjectInfo) extends DefaultProject(info) with ScriptedTestAssertTasks with IdeaProject {
  val slf4jVersion = "1.6.1"

  val libraryProject = project("library", "library", new Library(_))
  val userProject = project("user", "user", new User(_), libraryProject)

  class Library(info: ProjectInfo) extends DefaultProject(info) with IdeaProject {
    val slf4j = "org.slf4j" % "slf4j-api" % slf4jVersion % "test->default"
  }

  class User(info: ProjectInfo) extends DefaultProject(info) with IdeaProject {
    val slf4j = "org.slf4j" % "slf4j-api" % slf4jVersion
  }
}
