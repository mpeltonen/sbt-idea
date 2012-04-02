import org.sbtidea.test.util.AbstractScriptedTestBuild
import sbt._
import Keys.libraryDependencies

object ScriptedTestBuild extends AbstractScriptedTestBuild("dependency-with-classifier") {
  lazy val root = Project("main", file("."), settings = Defaults.defaultSettings ++ scriptedTestSettings ++ Seq(
    libraryDependencies += "com.github.scala-incubator.io" % "scala-io-core_2.9.1" % "0.3.0" classifier "test" intransitive()
  ))
}
