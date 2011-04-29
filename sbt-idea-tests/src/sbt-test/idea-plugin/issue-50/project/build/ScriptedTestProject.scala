import sbt._

class ScriptedTestProject(info: ProjectInfo) extends DefaultProject(info) with ScriptedTestAssertTasks with IdeaProject {
  val slf4j = "org.slf4j" % "slf4j-api" % "1.6.0"
  val slf4jsimple = "org.slf4j" % "slf4j-simple" % "1.6.0" % "test"
  val servletApi = "javax.servlet" % "servlet-api" % "2.5" % "provided" 
  val jettyWebApp = "org.eclipse.jetty" % "jetty-webapp" % "7.2.2.v20101205" % "test"
}
