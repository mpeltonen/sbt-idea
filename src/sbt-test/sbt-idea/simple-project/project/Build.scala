import org.sbtidea.test.util.AbstractScriptedTestBuild
import sbt._
import sbt.Keys._

object ScriptedTestBuild extends AbstractScriptedTestBuild("simple-project") {
  
  lazy val root = Project("main", file("."), settings = Defaults.defaultSettings ++ scriptedTestSettings ++ Seq(
    libraryDependencies ++= dependencies, scalacOptions ++= Seq("-unchecked", "-deprecation")
  ))

  lazy val dependencies = Seq(
    "junit" % "junit" % "4.8.2",
    "org.eclipse.jetty" % "jetty-server" % "7.0.0.v20091005" % "test",
    "javax.servlet" % "servlet-api" % "2.5" % "provided"
  )
}
