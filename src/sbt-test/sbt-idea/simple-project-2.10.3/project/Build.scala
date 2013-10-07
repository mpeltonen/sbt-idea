import org.sbtidea.test.util.AbstractScriptedTestBuild
import sbt._
import sbt.Keys._
import org.sbtidea.SbtIdeaPlugin._

object ScriptedTestBuild extends AbstractScriptedTestBuild("simple-project") {

  lazy val root = Project("main", file("."), settings = Defaults.defaultSettings ++ scriptedTestSettings ++ Seq(
    scalaVersion := "2.10.3"
  ))
}
