import org.sbtidea.test.util.AbstractScriptedTestBuild
import sbt._
import Keys.libraryDependencies

object ScriptedTestBuild extends AbstractScriptedTestBuild {
  lazy val root = Project("main", file("."), settings = Defaults.defaultSettings ++ scriptedTestSettings ++ Seq(
    libraryDependencies += "junit" % "junit" % "4.8.2"
  ))
}
