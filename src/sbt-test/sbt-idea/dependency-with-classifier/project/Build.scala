import org.sbtidea.test.util.AbstractScriptedTestBuild
import sbt._
import Keys.libraryDependencies

object ScriptedTestBuild extends AbstractScriptedTestBuild("dependency-with-classifier") {
  lazy val root = Project("main", file("."), settings = Defaults.defaultSettings ++ scriptedTestSettings ++ Seq(
    libraryDependencies ++= Seq(
      "com.googlecode.kiama" % "kiama_2.9.1" % "1.2.0" withSources() intransitive(),
      "com.googlecode.kiama" % "kiama_2.9.1" % "1.2.0" % "test" classifier "test" withSources() intransitive()
    )
  ))
}
