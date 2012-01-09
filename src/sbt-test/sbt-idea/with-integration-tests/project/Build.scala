import org.sbtidea.test.util.AbstractScriptedTestBuild
import sbt._
import Keys.libraryDependencies

object ScriptedTestBuild extends AbstractScriptedTestBuild("with-integration-tests") {
  lazy val root = Project("main", file("."), settings = Defaults.defaultSettings ++ scriptedTestSettings ++ Seq(
    libraryDependencies += "junit" % "junit" % "4.8.2"
  ))
    .configs( IntegrationTest )
    .settings( Defaults.itSettings : _*)
}
