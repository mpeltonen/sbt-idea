import org.sbtidea.test.util.AbstractScriptedTestBuild
import sbt._
import sbt.Keys._

object ScriptedTestBuild extends AbstractScriptedTestBuild("simple-project") {

  lazy val root = Project("main", file("."),
    settings = Defaults.defaultSettings ++
      scriptedTestSettings ++ Seq(libraryDependencies ++= dependencies)
  )

  lazy val dependencies = Seq(
    "junit" % "junit" % "4.8.2"
  )
}